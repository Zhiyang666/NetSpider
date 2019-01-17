package lianjianspider.controller;

import agent.IpAgent;
import lianjianspider.entity.IpAgentEntity;
import lianjianspider.repository.IpAgentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yuziyang
 */
@Slf4j
@RestController
@RequestMapping("/ipAgent/")
public class IpAgentSpiderController {

    @Autowired
    IpAgentRepository ipAgentRepository;

    public IpAgentSpiderController(IpAgentRepository ipAgentRepository){
        this.ipAgentRepository = ipAgentRepository;
    }

    @Async
    @GetMapping("start")
    public void start(String targetUrl){
        startSpiderIpAgent(targetUrl);
    }

    public void startSpiderIpAgent(String targetUrl){
        List<IpAgentEntity> agentEntities = new ArrayList<>();
        Date thisTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        IpAgent ipAgent = new IpAgent(targetUrl,5);
        String spiderTime = sdf.format(thisTime);
        //开始爬取ip代理
        ipAgent.start();

        //获取有效数据
        List<String> effectiveAgents = ipAgent.getEffectiveAgents();
        for (String string : effectiveAgents){
            IpAgentEntity ipAgentEntity = new IpAgentEntity();
            String[] ipAndPort = string.split("_");
            String ip = ipAndPort[0];
            Integer port = Integer.valueOf(ipAndPort[1]);
            ipAgentEntity.setIp(ip);
            ipAgentEntity.setPort(port);
            ipAgentEntity.setSpiderTime(spiderTime);
            ipAgentEntity.setTargetUrl(targetUrl);
            agentEntities.add(ipAgentEntity);
        }
        ipAgentRepository.saveAll(agentEntities);
    }
}
