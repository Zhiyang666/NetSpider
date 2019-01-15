package agent;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 国内免费代理获取,用于获取大量ip
 * 注意使用Jsoup的jar包不要使用1.8.3,此版本没有.proxy代理方法，现版本使用的是1.11.3
 *
 * @author yuziyang
 */
@Getter
@Setter
public class IpAgent {
    /**
     * c
     * 目标代理url:西刺代理
     * 免费代理，但是可用率很低
     */
    public static String agentUrl = "https://www.xicidaili.com/wt/";

    /**
     * 经过检查后有效的代理ip
     */
    private List<String> effectiveAgents = new ArrayList<>();


    /**
     * 最大页数
     */
    Integer maxPage = 0;
    /**
     * 测试目标网站
     */
    private String targetUrl;

    /**
     * 用于测试爬取的ip是否能使用
     *
     * @param url
     */
    public IpAgent(String url) {
        if (url != null) {
            this.targetUrl = url;
        }
    }

    public IpAgent() {

    }

    /**
     * 爬取表单分页URl最大页数
     */
    public List<String> spiderUrl(Document doc) {
        List<String> urlList = new ArrayList<>(2 >> 11);
        //获取底层最大页数element
        Element element = doc.selectFirst(".pagination");
        Elements elements = element.select("a");
        for (Element element1 : elements) {

            try {
                Integer page = Integer.valueOf(element1.ownText());
                if (page > maxPage) {
                    this.maxPage = page;
                }
            } catch (Exception e) {
                System.out.println("应该是格式化错误" + e);
            }
        }
        for (int i = 1; i <= maxPage; i++) {
            urlList.add(agentUrl + i);
        }
        return urlList;
    }

    /**
     * 爬取具体ip代理
     */
    public void spiderIp(Document doc, List<String> agentIp) {
        //获取大表单
        Element element = doc.selectFirst("#ip_list");
        //筛选出包含td的tr标签
        Elements trElements = element.select("tr:has(td)");
        for (Element singleElement : trElements) {
            //遍历tr标签，获取第1项:ip,第二项:端口
            Elements tdElements = singleElement.select("td");
            String ipAndPort = tdElements.get(1).text() + "_" + tdElements.get(2).text();
            System.out.println(ipAndPort);
            agentIp.add(ipAndPort);
        }
    }

    /**
     * 根据url加载html
     *
     * @param url
     * @return
     */
    public Document loadHtml(String url) {
        Document doc = null;
        try {
            //获取随机header
            Headers headers = new Headers();
            //jsoup加载
            doc = Jsoup.connect(url)
                    .userAgent(headers.getHeader())
                    .timeout(3000)
                    .header("Connection", "close")
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public Document loadHtml(String url, String ip, Integer port) throws Exception{
        if (ip == null || port == null) {
            return loadHtml(url);
        }
        Document doc = null;
        try {
            //获取随机header
            Headers headers = new Headers();
            //jsoup加载
            doc = Jsoup.connect(url)
                    .userAgent(headers.getHeader())
                    .timeout(10000)
                    .proxy(ip, port)
                    .header("Connection", "close")
                    .get();
        } catch (Exception e) {
            throw e;
        }
        return doc;
    }

    public void start() {
    }

    public static void main(String[] args) {
        IpAgent ipAgent = new IpAgent();
        //未检测过的ip
        List<String> agentIp = new ArrayList<>();
        Document doc = ipAgent.loadHtml(agentUrl);
        List<String> urlList = ipAgent.spiderUrl(doc);
        Integer spiderNum = 1;
        Integer useIpNum = 0;
        String ip = null;
        Integer port = null;
        for (String url : urlList) {
            Document pageDoc =null;
            try {
                while (pageDoc == null) {
                    try{
                        pageDoc = ipAgent.loadHtml(url, ip, port);
                        if(spiderNum ==0){
                            throw new Exception("此ip已爬取了最大爬取数,现在更换ip");
                        }

                    }catch (Exception e){
                        System.out.println(e.getMessage());
                        String[] ipAndPort = agentIp.get(useIpNum).split("_");
                        System.out.println("已更换Ip代理爬取:" + agentIp.get(useIpNum));
                        ip = ipAndPort[0];
                        port = Integer.valueOf(ipAndPort[1]);
                        spiderNum = 1;
                        useIpNum++;

                    }
                }
                Thread.sleep(3000);
                System.out.println("当前爬取的页面为:" + url);
                ipAgent.spiderIp(pageDoc, agentIp);
                spiderNum--;

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

}
