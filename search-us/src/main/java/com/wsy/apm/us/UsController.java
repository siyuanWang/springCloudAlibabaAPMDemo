package com.wsy.apm.us;


import com.wsy.apm.core.annotation.TransactionWithRemoteParent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/us")
public class UsController {

    @Autowired
    private UsService usService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UsController.class);

    @PostMapping(value = "/search", consumes = "application/json", produces = "application/json")
    @TransactionWithRemoteParent()
    public String searchForm(@RequestBody String req) {
        String result = usService.search(req);
        return result;
    }

    @GetMapping(value = "/enabled")
    public String check() {

        return "success";
    }
}
