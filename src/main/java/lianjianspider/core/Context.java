package lianjianspider.core;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 链家爬虫上下文,主要记录的是一些批处理公共信息
 * @author yuziyang
 */
@Getter
@Setter
public class Context {
    /**
     * 获取城市名的正则
     */
    private static final String CITY_REG = "(?<=https://)[\\w]*";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 城市名
     */
    private String city;

    /**
     * 爬取时间
     */
    private String spiderTime;

    /**
     * 是否售出
     */
    private Integer isSell;

    public Context(String url,Integer isSell){
        Matcher matcher=Pattern.compile(CITY_REG).matcher(url);
        matcher.find();
        this.city = matcher.group();
        Date thisTime = new Date();
        this.spiderTime = sdf.format(thisTime);
        this.isSell = isSell;
    }
}
