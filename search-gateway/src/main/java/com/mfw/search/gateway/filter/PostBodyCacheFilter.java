package com.mfw.search.gateway.filter;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Span;
import co.elastic.apm.api.Transaction;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mfw.search.gateway.constant.GatewayConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 缓存Post request body 过滤器
 * 缓存的 key:GatewayConstant.CACHE_POST_BODY
 */
public class PostBodyCacheFilter implements GlobalFilter, Ordered {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostBodyCacheFilter.class);
    private final List<HttpMessageReader<?>> messageReaders;
    private final static String TRANSACTION_ID = "transactionId";

    public PostBodyCacheFilter() {
        this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
    }

    private Map<String, String> decodeBody(String body) {
        return Arrays.stream(body.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
    }

    private Map<String, String> decodeJsonBody(String body) {
        return JSON.parseObject(body, new TypeReference<Map<String, String>>() {
        });
    }

    private String encodeBody(Map<String, String> map) {
        return map.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
    }

    private String encodeJsonBody(Map<String, String> map) {
        return JSON.toJSONString(map);
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod httpMethod = exchange.getRequest().getMethod();

        Transaction transaction = ElasticApm.startTransaction();
        transaction.setName("mainSearch");
        transaction.setType(Transaction.TYPE_REQUEST);

        Span span = transaction.startSpan("gateway", "filter", "gateway action");
        span.setName("com.mfw.search.gateway.filter.PostBodyCacheFilter#filter");

        LOGGER.info("APM埋点成功transactionId:{}", transaction.getId());

        if (HttpMethod.POST.equals(httpMethod)) {
            ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
            MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
            Mono<String> modifiedBody = serverRequest.bodyToMono(String.class).flatMap(body -> {
                if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {

                    //重要！获取到了body的数据，传给callback函数，做业务逻辑处理
                    Map<String, String> bodyMap = decodeBody(body);
                    exchange.getAttributes().put(GatewayConstant.CACHE_POST_BODY, bodyMap);
                    span.injectTraceHeaders((name, value) -> {
                        bodyMap.put(name, value);
                        LOGGER.info("APM埋点 key:{}, transactionId:{}", name, value);
                    });
                    span.end();
                    return Mono.just(encodeBody(bodyMap));
                } else if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                    // origin body map
                    Map<String, String> bodyMap = decodeJsonBody(body);
                    exchange.getAttributes().put(GatewayConstant.CACHE_POST_BODY, bodyMap);
                    //添加transactionId
                    span.injectTraceHeaders((name, value) -> {
                        bodyMap.put(name, value);
                        LOGGER.info("APM埋点 key:{}, transactionId:{}", name, value);
                    });
                    span.end();
                    return Mono.just(encodeJsonBody(bodyMap));
                }
                return Mono.empty();
            });

            BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(exchange.getRequest().getHeaders());

            // the new content type will be computed by bodyInserter
            // and then set in the request decorator
            headers.remove(HttpHeaders.CONTENT_LENGTH);

            CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
            return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
                ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {

                    public HttpHeaders getHeaders() {
                        long contentLength = headers.getContentLength();
                        HttpHeaders httpHeaders = new HttpHeaders();
                        httpHeaders.putAll(super.getHeaders());
                        if (contentLength > 0) {
                            httpHeaders.setContentLength(contentLength);
                        } else {
                            httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                        }
                        return httpHeaders;
                    }

                    public Flux<DataBuffer> getBody() {
                        return outputMessage.getBody();
                    }
                };
                return chain.filter(exchange.mutate().request(decorator).build()).then(Mono.fromRunnable(() -> transaction.end()));
            }));
        } else if (HttpMethod.GET.equals(httpMethod)) {
            span.injectTraceHeaders((name, value) -> {
                exchange.getRequest().getQueryParams().set(name, transaction.getId());
                LOGGER.info("APM埋点 key:{}, transactionId:{}", name, value);
            });
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                span.end();
                transaction.end();
                LOGGER.info("APM买点完成,transactionId:{}", transaction.getId());
            }));
        } else {
            //not support other Http Method
            exchange.getResponse().setStatusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            return exchange.getResponse().setComplete();
        }

    }

    @Override
    public int getOrder() {
        return GatewayConstant.POST_BODY_FILTER;
    }
}
