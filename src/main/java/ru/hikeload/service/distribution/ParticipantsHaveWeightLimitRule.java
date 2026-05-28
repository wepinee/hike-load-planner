package ru.hikeload.service.distribution;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hikeload.service.BusinessException;

@Component
@Order(20)
public class ParticipantsHaveWeightLimitRule implements DistributionRule {

    @Override
    public void check(DistributionContext context) {
        boolean allHaveLimit = context.getHike().getParticipants().stream()
                .allMatch(p -> p.getWeightLimit() != null && p.getWeightLimit().getMaxKg() > 0);
        if (!allHaveLimit) {
            throw new BusinessException("У всех участников должен быть указан максимальный вес");
        }
    }
}
