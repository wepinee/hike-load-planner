package ru.hikeload.service.distribution;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hikeload.domain.AssignmentType;
import ru.hikeload.domain.GearItem;

/**
 * Strategy: личное снаряжение закрепляется за владельцем.
 */
@Component
@Order(1)
public class PersonalGearDistributionStrategy implements LoadDistributionStrategy {

    private final AssignmentFactory assignmentFactory;

    public PersonalGearDistributionStrategy(AssignmentFactory assignmentFactory) {
        this.assignmentFactory = assignmentFactory;
    }

    @Override
    public void distribute(DistributionContext context) {
        int n = context.getParticipantCount();
        int days = context.getDays();

        for (GearItem item : context.getGearItems()) {
            if (!item.isPersonal()) {
                continue;
            }
            context.assign(assignmentFactory.forGear(
                    item,
                    item.getOwner(),
                    AssignmentType.GEAR_PERSONAL,
                    n,
                    days
            ));
        }
    }
}
