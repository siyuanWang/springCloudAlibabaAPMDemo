package com.wsy.apm.us;


import com.wsy.apm.as.AsService;
import com.wsy.apm.core.annotation.TransactionWithRemoteParent;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/us")
public class UsController {

    @Reference(version = "1.0.0", group = "default-search")
    private AsService asService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UsController.class);

    @PostMapping(value = "/search", consumes = "application/json", produces = "application/json")
    @TransactionWithRemoteParent()
    public String searchForm(@RequestBody String req) {
        String result = asService.helloAs(req);
        return result;
    }

    @GetMapping(value = "/enabled")
    public String check() {

        return "success";
    }
}
