package repositories;

import models.FeeCategory;
import models.enums.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeCategoryRepository extends JpaRepository<FeeCategory, Long> {
    List<FeeCategory> findByParentIsNull();
    FeeCategory findByNameAndParentIsNull(String name);

    List<FeeCategory> findByFeeTypeAndParentIsNull(FeeType feeType);
    List<FeeCategory> findByFeeTypeNotAndParentIsNull(FeeType feeType);

    @Query("SELECT f FROM FeeCategory f WHERE f.parent.name = :parentName AND f.parent.parent IS NULL")
    List<FeeCategory> findSubCategoriesOfTopLevel(@Param("parentName") String parentName);
}