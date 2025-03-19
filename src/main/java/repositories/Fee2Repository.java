package repositories;

import models.Fee2;
import models.FeeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Fee2Repository extends JpaRepository<Fee2, Long> {
    List<Fee2> findByFeeCategoryId(Long categoryId);
    List<Fee2> findAll();
}
