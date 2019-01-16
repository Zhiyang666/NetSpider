package lianjianspider.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * 房屋信息实体类,解析链家网页得到的房屋信息
 * @author yuziyang
 */
@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "property_info")
public class PropertyEntity {

    /**
     * 本表id,自增
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    /**
     * houseId 链家编号
     */
    @Column(name = "houseId")
    private Long houseId;
    /**
     * 城市
     */
    @Column(name = "city")
    private String city;

    /**
     * 总价,单位:万
     */
    @Column(name = "totalPrice")
    private Integer totalPrice;

    /**
     * 单价,单位:元/平米
     */
    @Column(name = "unitPrice")
    private Integer unitPrice;

    /**
     * 占地面积,单位:平方米
     */
    @Column(name = "area")
    private String area;

    /**
     * 所在楼层
     */
    @Column(name = "floor")
    private String floor;
    /**
     * 位置
     */
    @Column(name = "place")
    private String place;
    /**
     * 挂牌时间
     */
    @Column(name = "sellTime")
    private String sellTime;
    /**
     * 建造时间
     */
    @Column(name = "buildTime")
    private String buildTime;
    /**
     * 产权年限
     */
    @Column(name = "propertyYearNum")
    private Integer propertyYearNum;

    /**
     * 是否已售出
     * 0未售出 1已售出
     */
    @Column(name = "isSell")
    private Integer isSell;

    /**
     * 爬取时间
     */
    @Column(name = "spiderTime")
    private String spiderTime;

}
