package lianjianspider.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * 代理ip实体类,爬取的有效ip代理
 * @author yuziyang
 */
@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "ip_agent")
public class IpAgentEntity {
    /**
     * 本表id,自增
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    /**
     * 代理ip
     */
    @Column(name = "ip")
    private String ip;

    /**
     * 代理ip端口
     */
    @Column(name = "port")
    private Integer port;

    /**
     * 测试目标网址
     */
    @Column(name = "targetUrl")
    private String targetUrl;

    /**
     * 爬取时间
     */
    @Column(name = "spiderTime")
    private String spiderTime;
}
