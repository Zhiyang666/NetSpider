package firstspider;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这个类的实现基于网络博客，是学习别人的代码，并自己去理解
 */
public class FirstSpider {

    //获取img标签正则
    public static final String IMG_URL_REG = "<img.*src=.*>";


    public static final String IMG_URL_REG1= "<img.*src=(.*?)[^>]*?>";

    /**
     * 获取src路径的正则
     */
    private static final String IMG_SRC_REG = "(?<=src=//)[\\S]*";

    /**
     * 第一个方法，获取指定URL的html内容
     */
    public String getHtml(String url) throws Exception {
        URL url1 = new URL(url);
        //链接目标地址
        URLConnection connection = url1.openConnection();
        //获取流
        InputStream stream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(stream);
        //读到BufferedReader
        BufferedReader br = new BufferedReader(reader);

        String line;
        StringBuffer buffer = new StringBuffer();
        while (br.readLine() != null) {
            //读取一行，拼接到buffer中
            line = br.readLine();
            int lastIndex = 0;
            int index = 0;
            while ( line != null &&(index= line.indexOf(">", index + 1)) >= 0){
                String string =line.substring(lastIndex,index+1);
                buffer.append(string.trim());
                buffer.append("\n");
                lastIndex = index + 1;
            }
        }
        br.close();
        reader.close();
        stream.close();

        return buffer.toString();
    }

    public List<String> getSrc(String html){

        Matcher matcher=Pattern.compile(IMG_URL_REG1).matcher(html);
        List<String> list = new ArrayList<String>();
        while (matcher.find()){
            list.add(matcher.group());
            System.out.println(matcher.group());
        }
        return list;
    }


    public List<String> getImageSrc(List<String> listimageurl){
        List<String> listImageSrc=new ArrayList<String>();
        for (String image:listimageurl){
            Matcher matcher=Pattern.compile(IMG_SRC_REG).matcher(image);
            while (matcher.find()){
                System.out.println(matcher.group());
                listImageSrc.add(matcher.group());
            }
        }
        return listImageSrc;
    }

    public void Download(List<String> listImgSrc) {
        try {
            //开始时间
            Date begindate = new Date();
            for (String url : listImgSrc) {
                //开始时间
                Date begindate2 = new Date();
                String imageName = url.substring(url.lastIndexOf("/") + 1, url.length());
                if(!url.startsWith("https://")){
                    url = "https://" + url;
                }
                URL uri = new URL(url);
                InputStream in = uri.openStream();
                File file =new File("/Users/yuziyang/IdeaProjects/NetSpider/src/main/resources/img"+imageName);
                FileOutputStream fo = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int length = 0;
                System.out.println("开始下载:" + url);
                while ((length = in.read(buf, 0, buf.length)) != -1) {
                    fo.write(buf, 0, length);
                }
                in.close();
                fo.close();
                System.out.println(imageName + "下载完成");
                //结束时间
                Date overdate2 = new Date();
                double time = overdate2.getTime() - begindate2.getTime();
                System.out.println("耗时：" + time / 1000 + "s");
            }
            Date overdate = new Date();
            double time = overdate.getTime() - begindate.getTime();
            System.out.println("总耗时：" + time / 1000 + "s");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("下载失败");
        }
    }

}
