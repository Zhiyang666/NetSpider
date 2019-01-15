package lianjianspider.repository;

import lianjianspider.entity.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity,Integer> {

    /**
     * 查询总价,小于参数1,大于参数2
     * @param price1 最高价格
     * @param price2 最低价格
     * @return
     */
    List<PropertyEntity> findByTotalPriceLessThanAndTotalPriceGreaterThan(Integer price1,Integer price2);

    List<PropertyEntity> findByCityAndSpiderTimeAndIsSell(String city,String spiderTime,Integer isSell);


}