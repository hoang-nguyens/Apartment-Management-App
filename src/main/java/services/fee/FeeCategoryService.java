package services.fee;

import jakarta.persistence.EntityNotFoundException;
import models.fee.FeeCategory;
import models.enums.FeeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.fee.FeeCategoryRepository;

import java.util.List;

@Service
public class FeeCategoryService {
    @Autowired
    private FeeCategoryRepository feeCategoryRepository;

    @Autowired
    public FeeCategoryService(FeeCategoryRepository feeCategoryRepository) {
        this.feeCategoryRepository = feeCategoryRepository;
    }

    public FeeCategory createFeeCategory(String name, FeeCategory parent) {
        FeeCategory feeCategory = new FeeCategory(name, parent);
        return feeCategoryRepository.save(feeCategory);
    }

    public FeeCategory updateFeeCategory(Long feeCategoryId, String name) {
        FeeCategory feeCategory = feeCategoryRepository.findById(feeCategoryId).orElse(null);
        if (feeCategory == null) {
            throw new EntityNotFoundException("Fee category not found with id " + feeCategoryId);
        }
        feeCategory.setName(name);
        return feeCategoryRepository.save(feeCategory);
    }

    public List<FeeCategory> getAllFeeCategories() {
        return feeCategoryRepository.findAll();
    }

    public List<FeeCategory> getFeeCategoriesByType(FeeType type) {
        return feeCategoryRepository.findByFeeTypeAndParentIsNull(type);
    }

    public List<FeeCategory> getFeeCategoriesNotOptional() {
        return feeCategoryRepository.findByFeeTypeNotAndParentIsNull(FeeType.OPTIONAL);
    }

    public List<String> getSubCategoriesNames(String parentName) {
        return feeCategoryRepository.findSubCategoriesOfTopLevel(parentName)
                .stream()
                .map(FeeCategory::getName)
                .toList();
    }

    public List<String> getParentCategoryNames() {
        return feeCategoryRepository.findByParentIsNull()
                .stream()
                .map(FeeCategory::getName)
                .toList(); // Java 16+ (hoặc dùng .collect(Collectors.toList()) nếu Java cũ hơn)
    }

    public FeeCategory findTopLevelCategoryByName(String name){
        return feeCategoryRepository.findByNameAndParentIsNull(name);
    }
}