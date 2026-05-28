package ru.hikeload.service.distribution;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hikeload.domain.GearItem;
import ru.hikeload.service.BusinessException;

@Component
@Order(30)
public class PersonalGearOwnersRule implements DistributionRule {

    @Override
    public void check(DistributionContext context) {
        for (GearItem item : context.getGearItems()) {
            if (item.isPersonal() && item.getOwner() == null) {
                throw new BusinessException("У личного предмета «" + item.getName() + "» не указан владелец");
            }
        }
    }
}
