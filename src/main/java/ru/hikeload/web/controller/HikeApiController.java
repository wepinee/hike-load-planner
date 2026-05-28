package ru.hikeload.web.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import ru.hikeload.service.HikeService;
import ru.hikeload.web.dto.CreateHikeRequest;
import ru.hikeload.web.dto.HikeResponse;

@RestController
@RequestMapping("/api/hikes")
public class HikeApiController {

    private final HikeService hikeService;
    private final CurrentUserService currentUserService;

    public HikeApiController(HikeService hikeService, CurrentUserService currentUserService) {
        this.hikeService = hikeService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public Page<HikeResponse> list(@PageableDefault(size = 10, sort = "startDate") Pageable pageable) {
        return hikeService.listForUser(currentUserService.getCurrentUserId(), pageable);
    }

    @GetMapping("/{id}")
    public HikeResponse get(@PathVariable Long id) {
        return hikeService.getById(id, currentUserService.getCurrentUserId());
    }

    @GetMapping("/{id}/copy-sources")
    public Page<HikeResponse> copySources(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable
    ) {
        return hikeService.listCopySources(id, currentUserService.getCurrentUserId(), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HikeResponse create(@Valid @RequestBody CreateHikeRequest body) {
        return hikeService.create(body, currentUserService.getCurrentUserId());
    }

    @PostMapping("/{targetId}/gear/copy-from/{sourceId}")
    public HikeResponse copyGear(@PathVariable Long targetId, @PathVariable Long sourceId) {
        return hikeService.copyGearFromPreviousHike(
                targetId, sourceId, currentUserService.getCurrentUserId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        hikeService.delete(id, currentUserService.getCurrentUserId());
    }
}
