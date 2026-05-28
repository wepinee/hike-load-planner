package ru.hikeload.web.dto;

import ru.hikeload.domain.Gender;
import ru.hikeload.domain.Participant;
import ru.hikeload.domain.ParticipantRole;

public record ParticipantResponse(
        Long id,
        String name,
        String email,
        Gender gender,
        ParticipantRole role,
        Double maxWeightKg
) {
    public static ParticipantResponse from(Participant p) {
        return new ParticipantResponse(
                p.getId(),
                p.getName(),
                p.getEmail(),
                p.getGender(),
                p.getRole(),
                p.getWeightLimit().getMaxKg()
        );
    }
}
