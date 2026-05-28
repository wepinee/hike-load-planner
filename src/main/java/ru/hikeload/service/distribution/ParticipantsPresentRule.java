package ru.hikeload.service.distribution;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hikeload.service.BusinessException;

@Component
@Order(10)
public class ParticipantsPresentRule implements DistributionRule {

    @Override
    public void check(DistributionContext context) {
        if (context.getHike().getParticipants().isEmpty()) {
            throw new BusinessException("Добавьте участников в поход");
        }
    }
}
