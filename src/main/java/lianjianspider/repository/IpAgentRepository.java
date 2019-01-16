package lianjianspider.repository;

import lianjianspider.entity.IpAgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpAgentRepository extends JpaRepository<IpAgentEntity,Integer> {

}
