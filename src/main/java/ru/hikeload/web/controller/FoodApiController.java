package ru.hikeload.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hikeload.security.CurrentUserService;
import ru.hikeload.service.FoodService;
import ru.hikeload.web.dto.AddFoodItemRequest;
import ru.hikeload.web.dto.FoodItemResponse;
import ru.hikeload.web.dto.UpdateFoodItemRequest;

import java.util.List;

@RestController
@RequestMapping("/api/hikes/{hikeId}/food")
public class FoodApiController {

    private final FoodService foodService;
    private final CurrentUserService currentUserService;

    public FoodApiController(FoodService foodService, CurrentUserService currentUserService) {
        this.foodService = foodService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<FoodItemResponse> list(@PathVariable Long hikeId) {
        return foodService.listByHike(hikeId, currentUserService.getCurrentUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FoodItemResponse add(@PathVariable Long hikeId, @Valid @RequestBody AddFoodItemRequest body) {
        return foodService.add(hikeId, body, currentUserService.getCurrentUserId());
    }

    @org.springframework.web.bind.annotation.PutMapping("/{foodItemId}")
    public FoodItemResponse update(
            @PathVariable Long hikeId,
            @PathVariable Long foodItemId,
            @Valid @RequestBody UpdateFoodItemRequest body
    ) {
        return foodService.update(hikeId, foodItemId, body, currentUserService.getCurrentUserId());
    }

    @DeleteMapping("/{foodItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long hikeId, @PathVariable Long foodItemId) {
        foodService.delete(hikeId, foodItemId, currentUserService.getCurrentUserId());
    }
}
