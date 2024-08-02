package practice.newbalance.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import practice.newbalance.domain.item.Thumbnail;

@Repository
public interface ThumbnailRepository extends JpaRepository<Thumbnail, Long> {

}
