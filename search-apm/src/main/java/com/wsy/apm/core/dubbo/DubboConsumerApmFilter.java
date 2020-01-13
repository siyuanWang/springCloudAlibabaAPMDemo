package com.wsy.apm.core.dubbo;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Scope;
import co.elastic.apm.api.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Activate(group = "consumer")
public class DubboConsumerApmFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboConsumerApmFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Transaction transaction = ElasticApm.startTransactionWithRemoteParent(key -> {
            String traceId = invocation.getAttachment(key);
            LOGGER.info("key={},value={}", key, traceId);
            return traceId;
        });
        try (final Scope scope = transaction.activate()) {
            String name = "consumer:" + invocation.getInvoker().getInterface().getName() + "#" + invocation.getMethodName();
            transaction.setName(name);
            transaction.setType(Transaction.TYPE_REQUEST);

            Result result = invoker.invoke(invocation);

            return result;
        } catch (Exception e) {
            transaction.captureException(e);
            throw e;
        } finally {
            transaction.end();
        }
    }
}
