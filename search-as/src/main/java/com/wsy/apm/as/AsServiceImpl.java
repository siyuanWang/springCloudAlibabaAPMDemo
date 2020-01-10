package com.wsy.apm.as;

import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;

@Service(version = "1.0.0", group = "default-search")
public class AsServiceImpl implements AsService {

    @Reference(version = "1.0.0", group = "default-search")
    private BsService bsService;

    @Override
    public String helloAs(String str) {
        System.out.println("call as");
        return bsService.helloBs(str);
    }
}
