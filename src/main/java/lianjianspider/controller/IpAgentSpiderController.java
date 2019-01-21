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

    public IpAgentSpiderController(IpAgentRepository ipAgentRepository) {
        this.ipAgentRepository = ipAgentRepository;
    }

    @Async
    @GetMapping("start")
    public void start(String targetUrl) {
        startSpiderIpAgent(targetUrl);
    }

    public void startSpiderIpAgent(String targetUrl) {
        List<IpAgentEntity> agentEntities = new ArrayList<>();
        Date thisTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        IpAgent ipAgent = new IpAgent(targetUrl, 5);
        String spiderTime = sdf.format(thisTime);
        //开始爬取ip代理
        ipAgent.start();
        //每20条入库
        Integer startIndex = 0;
        //最后一次入库的时间
        Long lastTime = System.currentTimeMillis();
        //最后一次入库时list长度
        Integer lastSize = 0;
        while (true) {
            if (ipAgent.getEffectiveAgents().size() > 0 && ipAgent.getEffectiveAgents().size() >= startIndex + 20) {
                //获取有效数据
                List<String> effectiveAgents = ipAgent.getEffectiveAgents().subList(startIndex, startIndex + 20);
                for (String string : effectiveAgents) {
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
                startIndex = startIndex+20;
                lastTime = System.currentTimeMillis();
                lastSize = ipAgent.getEffectiveAgents().size();
            }
            //如果最后一次入库和现在list长度都没变化,并且一小时内都没入库
            if(startIndex + 20 > ipAgent.getEffectiveAgents().size() && (System.currentTimeMillis()-lastTime) >(1000 * 60 * 60)
            && lastSize == ipAgent.getEffectiveAgents().size()){
                break;
            }

        }


        ipAgentRepository.saveAll(agentEntities);
    }
}
