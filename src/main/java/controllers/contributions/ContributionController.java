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
            @RequestParam Long feeId,
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false, defaultValue = "Thu cong") String note
    ) {
        try {
            User resident = userService.getUserById(userId);
            Fee fee = feeService.getFeeById(feeId);
            System.out.println(fee.getId() + " " + resident.getId());
            return ResponseEntity.ok(contributionService.createContribution(resident, fee, amount, paymentMethod, note));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("users")
    public ResponseEntity<List<Contribution>> getAllContributions() {
        return ResponseEntity.ok(contributionService.getAllContributions());
    }

    @GetMapping("users/{id}")
    public ResponseEntity<List<Contribution>> getContributionByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(contributionService.getContributionsByUserId(id));
    }

}
