package ru.hikeload.service.distribution;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hikeload.domain.FoodItem;
import ru.hikeload.domain.Participant;

/**
 * Strategy: порции питания распределяются поровну между всеми участниками.
 */
@Component
@Order(3)
public class FoodDistributionStrategy implements LoadDistributionStrategy {

    private final AssignmentFactory assignmentFactory;

    public FoodDistributionStrategy(AssignmentFactory assignmentFactory) {
        this.assignmentFactory = assignmentFactory;
    }

    @Override
    public void distribute(DistributionContext context) {
        int n = context.getParticipantCount();
        int days = context.getDays();

        for (FoodItem food : context.getFoodItems()) {
            for (Participant participant : context.getHike().getParticipants()) {
                context.assign(assignmentFactory.forFood(food, participant, n, days));
            }
        }
    }
}
