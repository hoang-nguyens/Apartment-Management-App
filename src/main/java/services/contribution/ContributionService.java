package services.contribution;

import models.contibution.Contribution;
import models.fee.Fee;
import models.user.User;
import models.enums.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.contribution.ContributionRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ContributionService {
    private final ContributionRepository contributionRepository;

    @Autowired
    public ContributionService(ContributionRepository contributionRepository) {
        this.contributionRepository = contributionRepository;
    }

    public Contribution createContribution(Contribution contribution) {
        if (contribution.getPaymentMethod() == null){
            contribution.setPaymentMethod(PaymentMethod.CASH);
        }
        return contributionRepository.save(contribution);
    }

    public Contribution createContribution(User resident, Fee fee, BigDecimal amount, PaymentMethod paymentMethod, String note) {
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.CASH;
        }
        Contribution contribution = new Contribution(resident, fee, amount, paymentMethod, note);
        return contributionRepository.save(contribution);
    }

    public Contribution updateContribution(Contribution contribution) {
        return contributionRepository.save(contribution);
    }


    public List<Contribution> getAllContributions() {
        return contributionRepository.findAll();
    }

    public List<Contribution> getContributionsByUserId(Long userId) {
        return contributionRepository.findAllByUserId(userId);
    }
}