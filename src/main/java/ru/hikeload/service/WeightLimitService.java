package ru.hikeload.service;

import org.springframework.stereotype.Service;
import ru.hikeload.domain.Gender;
import ru.hikeload.web.dto.SuggestedWeightResponse;

@Service
public class WeightLimitService {

    public SuggestedWeightResponse suggest(Gender gender) {
        double suggested = gender == Gender.FEMALE ? 15.0 : 20.0;
        return new SuggestedWeightResponse(gender, suggested,
                "Рекомендуемый лимит для похода (кг), уточните под физическую форму");
    }
}
