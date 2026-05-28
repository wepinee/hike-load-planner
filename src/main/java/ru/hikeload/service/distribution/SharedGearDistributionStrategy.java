package ru.hikeload.service.distribution;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hikeload.domain.AssignmentType;
import ru.hikeload.domain.GearItem;
import ru.hikeload.domain.Participant;

import java.util.Comparator;

/**
 * Strategy: общее снаряжение — жадное распределение на участника с минимальной нагрузкой.
 */
@Component
@Order(2)
public class SharedGearDistributionStrategy implements LoadDistributionStrategy {

    private final AssignmentFactory assignmentFactory;

    public SharedGearDistributionStrategy(AssignmentFactory assignmentFactory) {
        this.assignmentFactory = assignmentFactory;
    }

    @Override
    public void distribute(DistributionContext context) {
        int n = context.getParticipantCount();
        int days = context.getDays();

        context.getGearItems().stream()
                .filter(GearItem::isShared)
                .sorted(Comparator.comparing(GearItem::getWeightKg).reversed())
                .forEach(item -> {
                    Participant target = context.leastLoadedParticipant();
                    context.assign(assignmentFactory.forGear(
                            item,
                            target,
                            AssignmentType.GEAR_SHARED,
                            n,
                            days
                    ));
                });
    }
}
