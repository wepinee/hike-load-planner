package ru.hikeload.service.distribution;

import ru.hikeload.domain.Assignment;
import ru.hikeload.domain.FoodItem;
import ru.hikeload.domain.GearItem;
import ru.hikeload.domain.Hike;
import ru.hikeload.domain.LoadPlan;
import ru.hikeload.domain.Participant;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контекст одного прохода формирования раскладки (общие данные для стратегий).
 */
public class DistributionContext {

    private final Hike hike;
    private final LoadPlan plan;
    private final Map<Long, Double> loads;
    private final List<GearItem> gearItems;
    private final List<FoodItem> foodItems;
    private final int participantCount;
    private final int days;

    public DistributionContext(
            Hike hike,
            LoadPlan plan,
            List<GearItem> gearItems,
            List<FoodItem> foodItems
    ) {
        this.hike = hike;
        this.plan = plan;
        this.gearItems = gearItems;
        this.foodItems = foodItems;
        this.participantCount = hike.getParticipants().size();
        this.days = hike.getDurationDays();
        this.loads = new HashMap<>();
        hike.getParticipants().forEach(p -> loads.put(p.getId(), 0.0));
    }

    public Hike getHike() {
        return hike;
    }

    public LoadPlan getPlan() {
        return plan;
    }

    public Map<Long, Double> getLoads() {
        return loads;
    }

    public List<GearItem> getGearItems() {
        return gearItems;
    }

    public List<FoodItem> getFoodItems() {
        return foodItems;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public int getDays() {
        return days;
    }

    public void assign(Assignment assignment) {
        plan.addAssignment(assignment);
        loads.merge(
                assignment.getParticipant().getId(),
                assignment.getEffectiveWeight(),
                Double::sum
        );
    }

    public Participant leastLoadedParticipant() {
        return hike.getParticipants().stream()
                .min(Comparator.comparingDouble(p -> loads.get(p.getId())))
                .orElseThrow();
    }

    public double totalGearWeight() {
        return gearItems.stream().mapToDouble(GearItem::getWeightKg).sum();
    }

    public double totalFoodWeight() {
        int n = Math.max(participantCount, 1);
        return foodItems.stream()
                .mapToDouble(f -> f.calculateTotalWeight(days, n))
                .sum();
    }

    public double totalCarryWeight() {
        return totalGearWeight() + totalFoodWeight();
    }

    public double totalCarryCapacity() {
        return hike.calculateTotalCarryCapacity();
    }
}
