package lianjianspider.controller;

import lianjianspider.core.Context;
import lianjianspider.core.LianJiaSpiderService;
import lianjianspider.repository.PropertyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yuziyang
 */
@Slf4j
@RestController
@RequestMapping("/property/")
public class PropertySpiderController {
    @Autowired
    LianJiaSpiderService lianJiaSpiderService;

    @Autowired
    PropertyRepository propertyRepository;

    @Async
    @GetMapping("start")
    public void start(String city){
        lianJiaSpiderService.start(city);
    }


    @GetMapping("test")
    public String test(){
        return "测试成功";
    }
}
