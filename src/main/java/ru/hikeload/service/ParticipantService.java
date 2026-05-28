package ru.hikeload.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hikeload.domain.Hike;
import ru.hikeload.domain.Participant;
import ru.hikeload.domain.ParticipantRole;
import ru.hikeload.domain.WeightLimit;
import ru.hikeload.repository.ParticipantRepository;
import ru.hikeload.repository.UserAccountRepository;
import ru.hikeload.web.dto.AddParticipantRequest;
import ru.hikeload.web.dto.ParticipantResponse;

import java.util.List;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final UserAccountRepository userAccountRepository;
    private final HikeService hikeService;

    public ParticipantService(
            ParticipantRepository participantRepository,
            UserAccountRepository userAccountRepository,
            HikeService hikeService
    ) {
        this.participantRepository = participantRepository;
        this.userAccountRepository = userAccountRepository;
        this.hikeService = hikeService;
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> listByHike(Long hikeId, Long userId) {
        hikeService.getAccessibleHike(hikeId, userId);
        return participantRepository.findByHikeId(hikeId).stream()
                .map(ParticipantResponse::from)
                .toList();
    }

    @Transactional
    public ParticipantResponse add(Long hikeId, AddParticipantRequest request, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        hikeService.ensureOrganizer(hike, userId);

        if (request.email() != null && participantRepository.existsByHikeIdAndEmail(hikeId, request.email())) {
            throw new BusinessException("Участник с таким email уже есть в походе");
        }

        Participant participant = new Participant();
        participant.setName(request.name().trim());
        participant.setEmail(request.email());
        participant.setGender(request.gender());
        participant.setRole(request.role() != null ? request.role() : ParticipantRole.PARTICIPANT);

        if (request.email() != null && !request.email().isBlank()) {
            userAccountRepository.findByEmail(request.email().trim().toLowerCase())
                    .ifPresent(participant::setUser);
        }

        WeightLimit limit = new WeightLimit();
        limit.setMaxKg(request.maxWeightKg());
        participant.setWeightLimit(limit);

        hike.addParticipant(participant);
        return ParticipantResponse.from(participantRepository.save(participant));
    }

    @Transactional
    public ParticipantResponse updateMaxWeight(
            Long hikeId,
            Long participantId,
            Double maxWeightKg,
            Long userId
    ) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        hikeService.ensureOrganizer(hike, userId);

        Participant participant = participantRepository.findByIdAndHikeId(participantId, hikeId)
                .orElseThrow(() -> new NotFoundException("Участник не найден"));

        if (maxWeightKg == null || maxWeightKg <= 0) {
            throw new BusinessException("Вес должен быть больше 0");
        }
        participant.getWeightLimit().setMaxKg(maxWeightKg);
        return ParticipantResponse.from(participantRepository.save(participant));
    }

    @Transactional
    public void remove(Long hikeId, Long participantId, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        hikeService.ensureOrganizer(hike, userId);

        Participant participant = participantRepository.findByIdAndHikeId(participantId, hikeId)
                .orElseThrow(() -> new NotFoundException("Участник не найден"));

        if (participant.getRole() == ParticipantRole.ORGANIZER) {
            throw new BusinessException("Нельзя удалить организатора похода");
        }

        hike.getParticipants().remove(participant);
        participantRepository.delete(participant);
    }
}
