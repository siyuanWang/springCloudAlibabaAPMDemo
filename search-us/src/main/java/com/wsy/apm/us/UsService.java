package com.wsy.apm.us;

import com.wsy.apm.as.AsService;
import com.wsy.apm.core.annotation.TransactionWithRemoteParent;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

@Service
public class UsService {

    @Reference(version = "1.0.0", group = "default-search")
    private AsService asService;

    @TransactionWithRemoteParent()
    public String search(String str) {
        return asService.helloAs(str);
    }
}
