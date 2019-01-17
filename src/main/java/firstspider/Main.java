package firstspider;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 主函数,用于测试和启动爬虫
 */
public class Main {
//    public static void main(String[] args){
//
//        String URL = "https://www.baidu.com";
//        FirstSpider spider = new FirstSpider();
//        try {
//           List<String> list = spider.getImageSrc(spider.getSrc(spider.getHtml(URL)));
//           spider.Download(list);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void main(String[] args){
        List<String> list = new ArrayList<>();
        for(int i = 0; i<5;i++){
            list.add(String.valueOf(i));
        }
        System.out.println(list.get(2));
        System.out.println(list);
        list.remove(2);
        System.out.println(list.get(2));
        System.out.println(list);
    }


}
