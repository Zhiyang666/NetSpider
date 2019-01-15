package firstspider;


import java.util.List;

/**
 * 主函数,用于测试和启动爬虫
 */
public class Main {
    public static void main(String[] args){

        String URL = "https://www.baidu.com";
        FirstSpider spider = new FirstSpider();
        try {
           List<String> list = spider.getImageSrc(spider.getSrc(spider.getHtml(URL)));
           spider.Download(list);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


}
