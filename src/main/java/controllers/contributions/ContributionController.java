package controllers.contributions;

import models.*;
import models.enums.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.ContributionService;
import services.FeeService;
import services.UserService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/contributions")
public class ContributionController {
    private final ContributionService contributionService;
    private final UserService userService;
    private final FeeService feeService;

    @Autowired
    public ContributionController(ContributionService contributionService, UserService userService, FeeService feeService) {
        this.contributionService = contributionService;
        this.userService = userService;
        this.feeService = feeService;
    }

    @PostMapping("/manual")
    public ResponseEntity<Contribution> manualPayment(
            @RequestBody Contribution contribution
    ) {
        try {
//            User resident = userService.getUserById(userId);
//            Fee fee = feeService.getFeeById(feeId);
//            System.out.println(fee.getId() + " " + resident.getId());
            return ResponseEntity.ok(contributionService.createContribution(contribution));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contribution> updateContribution(
            @PathVariable Long id,
            @RequestBody Contribution contribution
    ) {
        try {
            if (!id.equals(contribution.getId())) {
                return ResponseEntity.badRequest().build(); // ID không khớp => từ chối
            }
            Contribution updated = contributionService.updateContribution(contribution);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Contribution>> getAllContributions() {
        return ResponseEntity.ok(contributionService.getAllContributions());
    }

    @GetMapping("users/{id}")
    public ResponseEntity<List<Contribution>> getContributionByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(contributionService.getContributionsByUserId(id));
    }
}
