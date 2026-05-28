package ru.hikeload.service.distribution;

import org.springframework.stereotype.Component;
import ru.hikeload.domain.Assignment;
import ru.hikeload.domain.AssignmentType;
import ru.hikeload.domain.FoodItem;
import ru.hikeload.domain.GearItem;
import ru.hikeload.domain.Participant;

/**
 * Factory: создание {@link Assignment} для снаряжения и питания.
 */
@Component
public class AssignmentFactory {

    public Assignment forGear(
            GearItem item,
            Participant participant,
            AssignmentType type,
            int participantCount,
            int days
    ) {
        Assignment assignment = new Assignment();
        assignment.setAssignmentType(type);
        assignment.setItemName(item.getName());
        assignment.setGearItem(item);
        assignment.setParticipant(participant);
        assignment.setQuantity(1);
        assignment.recalculateWeight(participantCount, days);
        return assignment;
    }

    public Assignment forFood(
            FoodItem food,
            Participant participant,
            int participantCount,
            int days
    ) {
        Assignment assignment = new Assignment();
        assignment.setAssignmentType(AssignmentType.FOOD);
        assignment.setItemName(food.getName());
        assignment.setFoodItem(food);
        assignment.setParticipant(participant);
        assignment.setQuantity(1);
        assignment.recalculateWeight(participantCount, days);
        return assignment;
    }
}
