package ru.hikeload.service.distribution;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hikeload.service.BusinessException;

@Component
@Order(40)
public class TotalWeightWithinCapacityRule implements DistributionRule {

    @Override
    public void check(DistributionContext context) {
        double totalWeight = context.totalCarryWeight();
        double totalCapacity = context.totalCarryCapacity();
        if (totalWeight > totalCapacity) {
            throw new BusinessException(String.format(
                    "Невозможно распределить: общий вес %.1f кг превышает суммарный лимит %.1f кг",
                    totalWeight, totalCapacity
            ));
        }
    }
}
