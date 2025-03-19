package repositories;

import models.FeeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeCategoryRepository extends JpaRepository<FeeCategory, Long> {
    FeeCategory findByNameAndParentIsNull(String name);
    FeeCategory findByNameAndParent(String name, FeeCategory parent);
}
