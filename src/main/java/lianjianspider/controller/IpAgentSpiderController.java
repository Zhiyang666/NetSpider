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

        Date thisTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        IpAgent ipAgent = new IpAgent(targetUrl, 5);
        String spiderTime = sdf.format(thisTime);
        //起一个新线程监听有效ip入库
        new Thread() {
            @Override
            public void run() {
                //每20条入库
                int startIndex = 0;
                //最后一次入库的时间
                long lastTime = System.currentTimeMillis();
                //最后一次入库时list长度
                int lastSize = 0;
                while (true) {
                    log.info("开始检测入库条件,当前有效IP长度:{},上次入库时ip长度:{}",ipAgent.getEffectiveAgents().size(),startIndex);
                    //已入库标志
                    boolean tag = false;
                    if (ipAgent.getEffectiveAgents().size() >= startIndex + 20) {
                        List<IpAgentEntity> agentEntities = new ArrayList<>();
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
                        startIndex = startIndex + 20;
                        lastTime = System.currentTimeMillis();
                        lastSize = ipAgent.getEffectiveAgents().size();
                        ipAgentRepository.saveAll(agentEntities);
                        tag = true;
                    }
                    else if((System.currentTimeMillis() - lastTime) > (1000 * 60 * 60 * 2)){
                        //如果最后一次入库和现在list长度都没变化,并且2小时内都没入库
                        if (lastSize < ipAgent.getEffectiveAgents().size()) {
                            List<IpAgentEntity> agentEntities = new ArrayList<>();
                            //获取有效数据
                            List<String> effectiveAgents = ipAgent.getEffectiveAgents().subList(startIndex, ipAgent.getEffectiveAgents().size());
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
                            ipAgentRepository.saveAll(agentEntities);
                        }
                        break;
                    }
                    log.info("本次检测是否入库:{}",tag);
                    //每20秒检测入库一次
                    try {
                        this.sleep(20000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        //开始爬取ip代理
        ipAgent.start();


    }
}
