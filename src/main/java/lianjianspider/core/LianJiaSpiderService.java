package lianjianspider.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lianjianspider.entity.PropertyEntity;
import lianjianspider.repository.PropertyRepository;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 爬取核心逻辑
 * @author yuziyang
 */
@Service
@Getter
@Setter
public class LianJiaSpiderService {


    @Autowired
    PropertyRepository propertyRepository;

    Integer spiderNum = 0;

    /**
     * 存储链接超时报错URL
     */
    public List<String> errorUrl = new ArrayList<>(1>>8);
    /**
     * 存储房产信息的list
     */
    public List<PropertyEntity> propertyList = new ArrayList<>(1 >> 8);
    /**
     * 网页中存储一共多少页的class名
     */
    public static String PAGE_NUMBER_BOX = "house-lst-page-box";

    /**
     * 开始方法
     */
    public void start(String city){
        String URL = "https://"+city+".lianjia.com/";
        Context context = new Context(URL,0);
        deleteDuplicate(context);
        try {
            String secondHandUrl = findSecondHandPropertyUrl(URL);
            System.out.println("已查询到二手房链接地址:" + secondHandUrl);
            Long beginTime = System.currentTimeMillis();
            findHouseMessageUrl(secondHandUrl, beginTime,context);

            while (errorUrl.size()>0){
                List<String> newErrorUrl = new ArrayList();
                newErrorUrl.addAll(errorUrl);
                errorUrl.clear();
                for(String errorUrl1:newErrorUrl){
                    collectPropertyInfo(errorUrl1,context);
                }
            }
            propertyRepository.saveAll(propertyList);
            propertyList.clear();
            System.out.println("程序已经执行完毕,共记录"+getSpiderNum()+"条数据");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("程序执行失败,中止执行");
        }
    }
    /**
     * 找到二手房URL
     */
    public String findSecondHandPropertyUrl(String url) throws Exception{
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(3000)
                    .header("Connection", "close")
                    .get();
            Elements userElements = doc.getElementsByClass("typeUserInfo");
            for (Element element : userElements) {
                for (Element element1 : element.select(":containsOwn(二手房)")) {
                    url = element1.attr("href");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("查询二手房url失败");
        }
        return url;
    }

    /**
     * 根据二手房URL去查询房源链接，并返回数据
     */
    public String findHouseMessageUrl(String url, Long endtime,Context context) {
        Long beginTime = System.currentTimeMillis();
        System.out.println("用时" + (beginTime - endtime) + "毫秒");
        Integer maxPageNum = 0;
        Integer currentPageNum = 0;
        try {
            //获取链接html
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(3000)
                    .header("Connection", "close")
                    .get();
            for (Element element : doc.getElementsByClass(PAGE_NUMBER_BOX)) {
                System.out.println("-------------------------------");
                JSONObject jsonObject = JSON.parseObject(element.attr("page-data"));
                maxPageNum = jsonObject.getInteger("totalPage");
                currentPageNum = jsonObject.getInteger("curPage");
                System.out.println("共有" + maxPageNum + "页二手房源信息," + "当前处理页数:" + currentPageNum);
            }
            //查找其中的a标签的具体房源信息的url,并且收集数据
            for (Element aElement : doc.select("a.noresultRecommend")) {
                collectPropertyInfo(aElement.attr("href"),context);
                while (this.propertyList.size() >= 50){
                    propertyRepository.saveAll(this.propertyList);
                    this.propertyList.clear();
                }
            }
            if (currentPageNum < maxPageNum) {
                //构造新的页面url
                url = url.replaceFirst("ershoufang/.*", "ershoufang/pg" + (currentPageNum + 1) + "/");
                findHouseMessageUrl(url, beginTime,context);
            }
        } catch (Exception e) {
            System.out.println("加载二手房页面失败:"+e.getMessage()+",准备重新加载此页面");
            //如果加载失败,那么就重新加载
            findHouseMessageUrl(url,endtime,context);
        }
        return url;
    }

    /**
     * 根据具体房源url抓取房源信息
     *
     * @see lianjianspider.entity.PropertyEntity
     */
    public void collectPropertyInfo(String url,Context context){
        Long beginTime = System.currentTimeMillis();
        PropertyEntity propertyEntity = new PropertyEntity();
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(3000)
                    .header("Connection", "close")
                    .get();
            //优化:获取上方信息的div
            Element overviewContent = doc.select(".overview > .content").first();
            //优化:获取下方信息的div
            Element imContent = doc.select(".introContent").first();
            //获取id
            Element id = overviewContent.select("div.houseRecord > .info").first();
            //获取基本价格信息
            Element totalPrice =overviewContent.select("span.total").first();
            Element unitPrice = overviewContent.select("span.unitPriceValue").first();
            Element area = overviewContent.select("div.area > .mainInfo").first();
            Element place = overviewContent.select("div.areaName > .info").first();
            Element buildTime = overviewContent.select("div.area > .subInfo").first();
            Element floor = imContent.select("div.content li:contains(所在楼层)").first();
            Element propertyYear = imContent.select("div.content li:contains(产权年限)").first();
            //挂牌时间
            Element sellTime = imContent.select("div.content li:contains(挂牌时间)").first().child(1);

            propertyEntity.setHouseId(Long.valueOf(id.ownText()));
            propertyEntity.setTotalPrice(Double.valueOf(totalPrice.text()).intValue());
            propertyEntity.setUnitPrice(Integer.valueOf(unitPrice.ownText()));
            propertyEntity.setArea(area.text());
            propertyEntity.setPlace(place.text());
            propertyEntity.setBuildTime(buildTime.text());
            propertyEntity.setFloor(floor.ownText());
            Matcher matcher=Pattern.compile("\\d*").matcher(propertyYear.ownText());
            Integer propertyYearNum = 0;
            if(matcher.find()){
                propertyYearNum  = Integer.valueOf(matcher.group());
            }
            propertyEntity.setPropertyYearNum(propertyYearNum);
            propertyEntity.setSellTime(sellTime.text());

            //存储基本信息
            propertyEntity.setSpiderTime(context.getSpiderTime());
            propertyEntity.setCity(context.getCity());
            propertyEntity.setIsSell(context.getIsSell());
            propertyList.add(propertyEntity);
            Long endTime = System.currentTimeMillis();
            System.out.println("正在处理第"+(++spiderNum)+"条数据,用时"+(endTime-beginTime)+"毫秒");
        } catch (Exception e) {

            System.out.println(url+"链接超时,没关系,我们已经记录并重新加载");
            errorUrl.add(url);
        }
    }

    /**
     * 看当天是否已经爬取过数据,如果爬取过,就删除重新爬
     */
    public void deleteDuplicate(Context context){
        List<PropertyEntity> list = propertyRepository.findByCityAndSpiderTimeAndIsSell(context.getCity(),context.getSpiderTime(),context.getIsSell());
        if(list.size()>0){
            System.out.println("检测到已经爬取过重复数据即将进行删除操作 城市:"
                    +context.getCity() +" 爬取时间:"+context.getSpiderTime()
                    +" 是否交易:" +context.getIsSell());
            propertyRepository.deleteInBatch(list);
        }
    }


}
