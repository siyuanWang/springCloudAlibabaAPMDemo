package com.wsy.apm.core.dubbo;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Scope;
import co.elastic.apm.api.Transaction;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;


@Activate(group = "provider")
public class DubboProviderApmFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // use startTransactionWithRemoteParent to create transaction with parent, which id from prc context
        Transaction transaction = ElasticApm.startTransactionWithRemoteParent(key -> invocation.getAttachment(key));

        try (final Scope scope = transaction.activate()) {
            String[] interfaceArr = invocation.getAttachment("interface").split("\\.");
            String className = interfaceArr[interfaceArr.length - 1];
            String name = className + "#" + invocation.getMethodName();
            transaction.setName(name);
            transaction.setType(Transaction.TYPE_REQUEST);
            return invoker.invoke(invocation);
        } catch (Exception e) {
            transaction.captureException(e);
            throw e;
        } finally {
            transaction.end();
        }
    }
}
