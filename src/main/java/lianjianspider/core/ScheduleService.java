package lianjianspider.core;

import lianjianspider.controller.IpAgentSpiderController;
import lianjianspider.repository.IpAgentRepository;
import lianjianspider.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yuziyang
 * 定时任务
 */
@Component
public class ScheduleService {

    @Autowired
    LianJiaSpiderService lianJiaSpiderService;

    @Autowired
    IpAgentRepository ipAgentRepository;
    /**
     * 每天早晨7点启动定时爬虫任务,写死爬取南京数据
     */
    @Scheduled(cron = "0 0 7 * * ?")
    public void autoStartSpider(){
        lianJiaSpiderService.start("nj");
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void autoStartSpiderIpAgent(){
        IpAgentSpiderController ipAgentSpiderController = new IpAgentSpiderController(ipAgentRepository);
        ipAgentSpiderController.start("https://nj.lianjia.com/");
    }

}
