package ru.hikeload.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hikeload.domain.Gender;
import ru.hikeload.service.WeightLimitService;
import ru.hikeload.web.dto.SuggestedWeightResponse;

@RestController
@RequestMapping("/api/reference")
public class ReferenceApiController {

    private final WeightLimitService weightLimitService;

    public ReferenceApiController(WeightLimitService weightLimitService) {
        this.weightLimitService = weightLimitService;
    }

    @GetMapping("/suggested-weight")
    public SuggestedWeightResponse suggestedWeight(@RequestParam Gender gender) {
        return weightLimitService.suggest(gender);
    }
}
