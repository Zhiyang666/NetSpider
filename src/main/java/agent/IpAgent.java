package agent;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 国内免费代理获取,用于获取大量ip
 * 注意使用Jsoup的jar包不要使用1.8.3,此版本没有.proxy代理方法，现版本使用的是1.11.3
 *
 * @author yuziyang
 */
@Slf4j
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
     * 未检查的代理ip
     */
    private List<String> uncheckAgents = Collections.synchronizedList(new ArrayList<>());

    /**
     * 最大页数
     */
    Integer maxPage = 0;
    /**
     * 测试目标网站
     */
    private String targetUrl;

    /**
     * 每个ip爬取同一网站次数
     */
    private Integer spiderNumByIp;

    /**
     * 是否执行完爬取ip的标示(执行完爬取，并没有执行完检查)
     */
    Boolean finishSpider;

    /**
     * 开始
     */
    public void start() {
        this.finishSpider = false;
        IpAgent ipAgent = this;
        //每次检查的容量
        int capacity = 100;
        //线程数
        int threadNum = 20;
        for (int i = 0; i < threadNum; i++) {
            log.info("当前正在创建第{}个监听线程,请耐心等待。",i);
            int j = i;
            new Thread() {
                @Override
                public void run() {
                    Integer front = j * capacity;
                    Integer back = front + capacity;
                    while (true) {
                        Integer ipAgentLength = ipAgent.getUncheckAgents().size();
                        if (ipAgent.finishSpider) {
                            List<String> uncheckAgents = new ArrayList<>(ipAgent.getUncheckAgents().subList(front, ipAgent.getUncheckAgents().size() - 1));
                            ipAgent.checkUseful(uncheckAgents);
                            break;
                        }
                        if (ipAgentLength > back) {
                            List<String> uncheckAgents = new ArrayList<>(ipAgent.getUncheckAgents().subList(front, back));
                            ipAgent.checkUseful(uncheckAgents);
                            front = front + capacity * threadNum;
                            back = front + capacity;
                        }
                        try {
                            this.sleep(5000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
        this.startSpiderIp();
        this.finishSpider = true;
//        this.checkUseful(this.uncheckAgents);
    }

    /**
     * 用于测试爬取的ip是否能使用的目标网址
     *
     * @param url
     * @param spiderNumByIp 每个ip爬取同一网站次数
     */
    public IpAgent(String url, Integer spiderNumByIp) {
        if (url != null && spiderNumByIp != null) {
            this.targetUrl = url;
            this.spiderNumByIp = spiderNumByIp;
        } else {
            this.targetUrl = agentUrl;
            this.spiderNumByIp = 5;
        }
    }

    public IpAgent() {
        this.targetUrl = agentUrl;
        this.spiderNumByIp = 5;
    }

    /**
     * 检查爬取的ip是否有效,有效的持久化
     *
     * @param agentIpList
     */
    public void checkUseful(List<String> agentIpList) {
        if (agentIpList == null) {
            return;
        }
        String ip;
        Integer port;
        Integer totalNum = agentIpList.size();
        Integer usefulNum = 0;
        Integer unUsefulNum = 0;
        for (String string : agentIpList) {
            String[] ipAndPort = string.split("_");
            ip = ipAndPort[0];
            port = Integer.valueOf(ipAndPort[1]);
            Document doc = null;
            //如果此ip测试目标网站成功，那么就加入到有效ip的list中
            try {
                //测试目标网站
                doc = loadHtml(targetUrl, ip, port);
            } catch (Exception e) {
                log.info("此ip无效:" + string);
                unUsefulNum++;
            } finally {
                if (doc != null) {
                    effectiveAgents.add(string);
                    usefulNum++;
                }
            }
        }
        log.info("检查并筛选完成,ip总条数:{},有效条数:{},无效条数:{}", totalNum, usefulNum, unUsefulNum);
    }

    /**
     * 爬取表单分页URl的最大页数，用于构造下一个爬取页面的URL
     */
    private List<String> spiderUrl(Document doc) {
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
                log.warn("应该是格式化错误,不会影响本程序-->" + e);
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
    private void spiderIp(Document doc) {
        //获取大表单
        Element element = doc.selectFirst("#ip_list");
        //筛选出包含td的tr标签
        Elements trElements = element.select("tr:has(td)");
        for (Element singleElement : trElements) {
            //遍历tr标签，获取第1项:ip,第二项:端口
            Elements tdElements = singleElement.select("td");
            String ipAndPort = tdElements.get(1).text() + "_" + tdElements.get(2).text();
            log.info("爬取的ip_host为:{}", ipAndPort);
            uncheckAgents.add(ipAndPort);
        }
    }

    /**
     * 根据url加载html
     *
     * @param url
     * @return
     */
    private Document loadHtml(String url) {
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
     *
     * @param url
     * @param ip
     * @param port
     * @return
     * @throws Exception
     */
    private Document loadHtml(String url, String ip, Integer port) throws Exception {
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

    /**
     * 开始爬取ip
     *
     * @return
     */
    private void startSpiderIp() {
        Document doc = loadHtml(agentUrl);
        List<String> urlList = spiderUrl(doc);
        Integer spiderNum = spiderNumByIp;
        //当前已经使用未检查ip池ip的个数
        Integer usedUncheckIpNum = 0;
        //当前已经使用有效ip池ip的个数
        Integer usedEffectiveIpNum = 0;
        String ip = null;
        Integer port = null;
        A:
        for (String url : urlList) {
            //每一页必将会被爬取
            Document pageDoc = null;
            //记录一个页面报错的次数
            Integer thisUrlErrorCount = 0;
            while (pageDoc == null) {
                try {
                    Thread.sleep(3000);
                    log.info("当前爬取的页面为:" + url);
                    pageDoc = loadHtml(url, ip, port);
                    if (spiderNum == 0) {
                        throw new Exception("此ip已爬取了最大爬取数,现在更换ip");
                    }
                } catch (Exception e) {
                    //超过20次就停止搜索这个页面
                    if (thisUrlErrorCount >= 20) {
                        continue A;
                    }
                    thisUrlErrorCount++;
                    log.warn(e.getMessage());
                    //修改,优先使用有效ip爬取
                    String[] ipAndPort;
                    if (effectiveAgents.size() > usedEffectiveIpNum) {
                        ipAndPort = effectiveAgents.get(usedEffectiveIpNum).split("_");
                        log.warn("已更换使用检查有效Ip代理爬取:{},当前正在使用第{}条有效ip", effectiveAgents.get(usedEffectiveIpNum), usedEffectiveIpNum++);
                    } else {
                        ipAndPort = uncheckAgents.get(usedUncheckIpNum).split("_");
                        log.warn("已更换使用未检查Ip代理爬取:{},当前正在使用第{}条未检查ip", uncheckAgents.get(usedUncheckIpNum), usedUncheckIpNum++);
                    }
                    ip = ipAndPort[0];
                    port = Integer.valueOf(ipAndPort[1]);
                    //重置当前这个ip的剩余应加载条数
                    spiderNum = spiderNumByIp;
                }
            }
            spiderIp(pageDoc);
            spiderNum--;
        }
    }
}
