package ru.hikeload.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hikeload.security.CurrentUserService;
import ru.hikeload.service.ParticipantService;
import ru.hikeload.web.dto.AddParticipantRequest;
import ru.hikeload.web.dto.ParticipantResponse;
import ru.hikeload.web.dto.UpdateParticipantWeightRequest;

import java.util.List;

@RestController
@RequestMapping("/api/hikes/{hikeId}/participants")
public class ParticipantApiController {

    private final ParticipantService participantService;
    private final CurrentUserService currentUserService;

    public ParticipantApiController(
            ParticipantService participantService,
            CurrentUserService currentUserService
    ) {
        this.participantService = participantService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ParticipantResponse> list(@PathVariable Long hikeId) {
        return participantService.listByHike(hikeId, currentUserService.getCurrentUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantResponse add(@PathVariable Long hikeId, @Valid @RequestBody AddParticipantRequest body) {
        return participantService.add(hikeId, body, currentUserService.getCurrentUserId());
    }

    @PatchMapping("/{participantId}/max-weight")
    public ParticipantResponse updateMaxWeight(
            @PathVariable Long hikeId,
            @PathVariable Long participantId,
            @Valid @RequestBody UpdateParticipantWeightRequest body
    ) {
        return participantService.updateMaxWeight(
                hikeId, participantId, body.maxWeightKg(), currentUserService.getCurrentUserId());
    }

    @DeleteMapping("/{participantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long hikeId, @PathVariable Long participantId) {
        participantService.remove(hikeId, participantId, currentUserService.getCurrentUserId());
    }
}
