package ru.hikeload.service.distribution;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Запускает цепочку правил (Specification / Policy) перед распределением.
 */
@Component
public class DistributionValidator {

    private final List<DistributionRule> rules;

    public DistributionValidator(List<DistributionRule> rules) {
        this.rules = rules;
    }

    public void validate(DistributionContext context) {
        for (DistributionRule rule : rules) {
            rule.check(context);
        }
    }
}
