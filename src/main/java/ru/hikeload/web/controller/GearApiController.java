package ru.hikeload.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hikeload.security.CurrentUserService;
import ru.hikeload.service.GearService;
import ru.hikeload.web.dto.AddGearItemRequest;
import ru.hikeload.web.dto.GearItemResponse;
import ru.hikeload.web.dto.UpdateGearItemRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hikes/{hikeId}/gear")
public class GearApiController {

    private final GearService gearService;
    private final CurrentUserService currentUserService;

    public GearApiController(GearService gearService, CurrentUserService currentUserService) {
        this.gearService = gearService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<GearItemResponse> list(@PathVariable Long hikeId) {
        return gearService.listByHike(hikeId, currentUserService.getCurrentUserId());
    }

    @PostMapping("/shared")
    @ResponseStatus(HttpStatus.CREATED)
    public GearItemResponse addShared(@PathVariable Long hikeId, @Valid @RequestBody AddGearItemRequest body) {
        return gearService.addShared(hikeId, body, currentUserService.getCurrentUserId());
    }

    @PostMapping("/personal")
    @ResponseStatus(HttpStatus.CREATED)
    public GearItemResponse addPersonal(@PathVariable Long hikeId, @Valid @RequestBody AddGearItemRequest body) {
        return gearService.addPersonal(hikeId, body, currentUserService.getCurrentUserId());
    }

    @PutMapping("/{gearItemId}")
    public GearItemResponse update(
            @PathVariable Long hikeId,
            @PathVariable Long gearItemId,
            @Valid @RequestBody UpdateGearItemRequest body
    ) {
        return gearService.update(hikeId, gearItemId, body, currentUserService.getCurrentUserId());
    }

    @PatchMapping("/{gearItemId}/weight")
    public GearItemResponse updateWeight(
            @PathVariable Long hikeId,
            @PathVariable Long gearItemId,
            @RequestBody Map<String, Double> body
    ) {
        return gearService.updateWeight(gearItemId, body.get("weightKg"), currentUserService.getCurrentUserId());
    }

    @DeleteMapping("/{gearItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long hikeId, @PathVariable Long gearItemId) {
        gearService.delete(hikeId, gearItemId, currentUserService.getCurrentUserId());
    }
}
