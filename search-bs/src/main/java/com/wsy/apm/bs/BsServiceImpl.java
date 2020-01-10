package com.wsy.apm.bs;

import com.wsy.apm.as.BsService;
import org.apache.dubbo.config.annotation.Service;

@Service(version = "1.0.0", group = "default-search")
public class BsServiceImpl implements BsService {

    @Override
    public String helloBs(String str) {
        System.out.println("call bs");
        return "hello " + str;
    }
}
