package ru.hikeload.web.dto;

import ru.hikeload.domain.FoodItem;

public record FoodItemResponse(
        Long id,
        String name,
        Double weightPerPortionKg,
        Integer caloriesPerPortion,
        Double portionsPerPersonPerDay,
        Double totalWeightKg,
        Integer totalCalories
) {
    public static FoodItemResponse from(FoodItem item, int durationDays, int participantCount) {
        return new FoodItemResponse(
                item.getId(),
                item.getName(),
                item.getWeightPerPortionKg(),
                item.getCaloriesPerPortion(),
                item.getPortionsPerPersonPerDay(),
                item.calculateTotalWeight(durationDays, participantCount),
                item.calculateTotalCalories(durationDays, participantCount)
        );
    }
}
