package ru.hikeload.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hikeload.security.CurrentUserService;
import ru.hikeload.service.DistributionService;
import ru.hikeload.web.dto.LoadPlanExportResponse;
import ru.hikeload.web.dto.LoadPlanResponse;
import ru.hikeload.web.dto.ReassignItemRequest;

@RestController
@RequestMapping("/api/hikes/{hikeId}/load-plan")
public class LoadPlanApiController {

    private final DistributionService distributionService;
    private final CurrentUserService currentUserService;

    public LoadPlanApiController(
            DistributionService distributionService,
            CurrentUserService currentUserService
    ) {
        this.distributionService = distributionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public LoadPlanResponse generate(@PathVariable Long hikeId) {
        return distributionService.generate(hikeId, currentUserService.getCurrentUserId());
    }

    @GetMapping
    public LoadPlanResponse get(@PathVariable Long hikeId) {
        return distributionService.get(hikeId, currentUserService.getCurrentUserId());
    }

    @GetMapping("/my")
    public LoadPlanResponse my(@PathVariable Long hikeId) {
        return distributionService.getMyLoad(hikeId, currentUserService.getCurrentUserId());
    }

    @GetMapping("/export")
    public LoadPlanExportResponse export(@PathVariable Long hikeId) {
        return distributionService.export(hikeId, currentUserService.getCurrentUserId());
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long hikeId) {
        byte[] pdf = distributionService.exportPdf(hikeId, currentUserService.getCurrentUserId());
        String filename = "load-plan-" + hikeId + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PutMapping("/reassign")
    public LoadPlanResponse reassign(
            @PathVariable Long hikeId,
            @Valid @RequestBody ReassignItemRequest body
    ) {
        return distributionService.reassign(hikeId, body, currentUserService.getCurrentUserId());
    }
}
