package ru.hikeload.web.dto;

import ru.hikeload.domain.GearItem;
import ru.hikeload.domain.ItemType;

public record GearItemResponse(
        Long id,
        String name,
        Double weightKg,
        ItemType type,
        Long ownerParticipantId
) {
    public static GearItemResponse from(GearItem item) {
        return new GearItemResponse(
                item.getId(),
                item.getName(),
                item.getWeightKg(),
                item.getType(),
                item.getOwner() != null ? item.getOwner().getId() : null
        );
    }
}
