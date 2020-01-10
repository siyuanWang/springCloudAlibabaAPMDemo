package com.wsy.apm.core.aop;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wsy.apm.core.annotation.TransactionWithRemoteParent;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

@Aspect
public class ApmAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApmAspect.class);

    @PostConstruct
    private void init() {
        LOGGER.info("ApmAspect加载完毕");
    }


    @Pointcut(value = "@annotation(transactionWithRemoteParent)", argNames = "transactionWithRemoteParent")
    public void pointcut(TransactionWithRemoteParent transactionWithRemoteParent) {

    }

    @Around(value = "pointcut(transactionWithRemoteParent)", argNames = "joinPoint,transactionWithRemoteParent")
    public Object around(ProceedingJoinPoint joinPoint, TransactionWithRemoteParent transactionWithRemoteParent) throws Throwable {
        Transaction transaction = null;
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            transaction = ElasticApm.startTransactionWithRemoteParent(key -> {
                String httpRequest = (String) joinPoint.getArgs()[0];
                JSONObject json = JSON.parseObject(httpRequest);
                String traceId = json.getString(key);
                LOGGER.info("切面添加了子Transaction，key={},value={}", key, traceId);
                RpcContext.getContext().setAttachment(key, traceId);
                return traceId;
            });
            transaction.setName(StringUtils.isNotBlank(transactionWithRemoteParent.name())
                    ? transactionWithRemoteParent.name() : signature.getName());
            transaction.setType(Transaction.TYPE_REQUEST);
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            if (transaction != null) {
                transaction.captureException(throwable);
            }
            throw throwable;
        } finally {
            if (transaction != null) {
                LOGGER.info("切面执行完毕，上报Transaction:{}", transaction.getId());
                transaction.end();
            }
        }
    }
}
