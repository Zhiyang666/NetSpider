package lianjiaspider;

import lianjianspider.SpiderApplication;
import lianjianspider.controller.IpAgentSpiderController;
import lianjianspider.core.Context;
import lianjianspider.core.LianJiaSpiderService;
import lianjianspider.entity.PropertyEntity;
import lianjianspider.repository.IpAgentRepository;
import lianjianspider.repository.PropertyRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {SpiderApplication.class})
public class test {

    @Autowired
    LianJiaSpiderService lianJiaSpiderService;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    IpAgentRepository ipAgentRepository;

    @Test
    public void test() {

        String url = "https://nj.lianjia.com/";
//        Context context = new Context(URL,0);
//        lianJiaSpiderService.deleteDuplicate(context);
//        String secondHandUrl;
//        try {
//            secondHandUrl = lianJiaSpiderService.findSecondHandPropertyUrl(URL);
//            System.out.println("已查询到二手房链接地址:" + secondHandUrl);
//            Long beginTime = System.currentTimeMillis();
//            lianJiaSpiderService.findHouseMessageUrl(secondHandUrl, beginTime,context);
//            while (lianJiaSpiderService.errorUrl.size()>0){
//                List<String> newErrorUrl = new ArrayList();
//                newErrorUrl.addAll(lianJiaSpiderService.errorUrl);
//                lianJiaSpiderService.errorUrl.clear();
//                for(String erroeUrl1:newErrorUrl){
//                    lianJiaSpiderService.collectPropertyInfo(erroeUrl1,context);
//                }
//            }
//            propertyRepository.saveAll(lianJiaSpiderService.propertyList);
//            lianJiaSpiderService.propertyList.clear();
//            System.out.println("程序已经执行完毕,共记录"+lianJiaSpiderService.getSpiderNum()+"条数据");
//        }catch (Exception e){
//            e.printStackTrace();
//            System.out.println("程序执行失败,中止执行");
//        }
        IpAgentSpiderController ipAgentSpiderController = new IpAgentSpiderController(ipAgentRepository);
        ipAgentSpiderController.start(url);

    }


}
