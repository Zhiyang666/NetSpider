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
     * 用于测试爬取的ip是否能使用的目标网址
     * @param url
     */
    public IpAgent(String url) {
        if (url != null) {
            this.targetUrl = url;
        }
    }

    public IpAgent() {
        this.targetUrl = agentUrl;
    }

    /**
     * 检查爬取的ip是否有效,有效的持久化
     * @param agentIpList
     */
    public void checkUseful(List<String> agentIpList){
        String ip = null;
        Integer port = null;

        for(String string: agentIpList){

            String[] ipAndPort = string.split("_");
            ip = ipAndPort[0];
            port = Integer.valueOf(ipAndPort[1]);
            Document doc = null;
            //如果此ip测试目标网站成功，那么就加入到有效ip的list中
            try {
                doc = loadHtml(targetUrl,ip,port);
                if(doc != null){
                    effectiveAgents.add(string);
                }
            }catch (Exception e){
                System.out.println("此ip无效:"+string);
            }
        }


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

    /**
     * 由于是代理加载,所以可以适当延时，否则容易报超时异常
     * @param url
     * @param ip
     * @param port
     * @return
     * @throws Exception
     */
    public Document loadHtml(String url, String ip, Integer port) throws Exception{
        if (ip == null || port == null) {
            return loadHtml(url);
        }
        Document doc = null;
        try {
            //获取随机header
            Headers headers = new Headers();
            //设置proxy代理
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
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

    public List<String> startSpiderIp() {
        //未检测过的ip
        List<String> agentIpList = new ArrayList<>();
        Document doc = loadHtml(agentUrl);
        List<String> urlList = spiderUrl(doc);
        Integer spiderNum = 1;
        Integer useIpNum = 0;
        String ip = null;
        Integer port = null;
        for (String url : urlList) {
            Document pageDoc =null;
            try {
                while (pageDoc == null) {
                    try{
                        pageDoc = loadHtml(url, ip, port);
                        if(spiderNum ==0){
                            throw new Exception("此ip已爬取了最大爬取数,现在更换ip");
                        }

                    }catch (Exception e){
                        System.out.println(e.getMessage());
                        String[] ipAndPort = agentIpList.get(useIpNum).split("_");
                        System.out.println("已更换Ip代理爬取:" + agentIpList.get(useIpNum));
                        ip = ipAndPort[0];
                        port = Integer.valueOf(ipAndPort[1]);
                        spiderNum = 1;
                        useIpNum++;
                    }
                }
                Thread.sleep(3000);
                System.out.println("当前爬取的页面为:" + url);
                spiderIp(pageDoc, agentIpList);
                spiderNum--;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return agentIpList;
    }

    public static void main(String[] args) {
        IpAgent ipAgent = new IpAgent();
        List<String> ipList = ipAgent.startSpiderIp();
        ipAgent.checkUseful(ipList);
    }

}
