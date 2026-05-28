package ru.hikeload.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hikeload.domain.FoodItem;
import ru.hikeload.domain.Hike;
import ru.hikeload.repository.AssignmentRepository;
import ru.hikeload.repository.FoodItemRepository;
import ru.hikeload.web.dto.AddFoodItemRequest;
import ru.hikeload.web.dto.FoodItemResponse;
import ru.hikeload.web.dto.UpdateFoodItemRequest;

import java.util.List;

@Service
public class FoodService {

    private final FoodItemRepository foodItemRepository;
    private final AssignmentRepository assignmentRepository;
    private final HikeService hikeService;

    public FoodService(
            FoodItemRepository foodItemRepository,
            AssignmentRepository assignmentRepository,
            HikeService hikeService
    ) {
        this.foodItemRepository = foodItemRepository;
        this.assignmentRepository = assignmentRepository;
        this.hikeService = hikeService;
    }

    @Transactional(readOnly = true)
    public List<FoodItemResponse> listByHike(Long hikeId, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        int n = hike.getParticipants().size();
        int days = hike.getDurationDays();
        return foodItemRepository.findByHikeIdOrderByNameAsc(hikeId).stream()
                .map(f -> FoodItemResponse.from(f, days, Math.max(n, 1)))
                .toList();
    }

    @Transactional
    public FoodItemResponse add(Long hikeId, AddFoodItemRequest request, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        hikeService.ensureOrganizer(hike, userId);

        FoodItem item = new FoodItem();
        item.setName(request.name().trim());
        item.setWeightPerPortionKg(request.weightPerPortionKg());
        item.setCaloriesPerPortion(request.caloriesPerPortion());
        item.setPortionsPerPersonPerDay(request.portionsPerPersonPerDay());
        item.setHike(hike);

        FoodItem saved = foodItemRepository.save(item);
        int n = Math.max(hike.getParticipants().size(), 1);
        return FoodItemResponse.from(saved, hike.getDurationDays(), n);
    }

    @Transactional
    public FoodItemResponse update(Long hikeId, Long foodItemId, UpdateFoodItemRequest request, Long userId) {
        FoodItem item = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));
        if (!item.getHike().getId().equals(hikeId)) {
            throw new NotFoundException("Продукт не найден");
        }
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        hikeService.ensureOrganizer(hike, userId);

        item.setName(request.name().trim());
        item.setWeightPerPortionKg(request.weightPerPortionKg());
        item.setCaloriesPerPortion(request.caloriesPerPortion());
        item.setPortionsPerPersonPerDay(request.portionsPerPersonPerDay());

        FoodItem saved = foodItemRepository.save(item);
        int n = Math.max(hike.getParticipants().size(), 1);
        return FoodItemResponse.from(saved, hike.getDurationDays(), n);
    }

    @Transactional
    public void delete(Long hikeId, Long foodItemId, Long userId) {
        FoodItem item = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));
        if (!item.getHike().getId().equals(hikeId)) {
            throw new NotFoundException("Продукт не найден");
        }
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        hikeService.ensureOrganizer(hike, userId);
        assignmentRepository.deleteByFoodItemId(foodItemId);
        foodItemRepository.delete(item);
    }
}
