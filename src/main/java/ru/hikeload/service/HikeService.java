package ru.hikeload.service;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hikeload.domain.GearItem;
import ru.hikeload.domain.Gender;
import ru.hikeload.domain.Hike;
import ru.hikeload.domain.HikeStatus;
import ru.hikeload.domain.ItemType;
import ru.hikeload.domain.Participant;
import ru.hikeload.domain.ParticipantRole;
import ru.hikeload.domain.UserAccount;
import ru.hikeload.domain.WeightLimit;
import ru.hikeload.repository.AssignmentRepository;
import ru.hikeload.repository.FoodItemRepository;
import ru.hikeload.repository.GearItemRepository;
import ru.hikeload.repository.HikeRepository;
import ru.hikeload.repository.LoadPlanRepository;
import ru.hikeload.repository.ParticipantRepository;
import ru.hikeload.repository.UserAccountRepository;
import ru.hikeload.web.dto.CreateHikeRequest;
import ru.hikeload.web.dto.HikeResponse;

@Service
public class HikeService {

    private final HikeRepository hikeRepository;
    private final UserAccountRepository userAccountRepository;
    private final GearItemRepository gearItemRepository;
    private final AssignmentRepository assignmentRepository;
    private final LoadPlanRepository loadPlanRepository;
    private final FoodItemRepository foodItemRepository;
    private final ParticipantRepository participantRepository;
    private final EntityManager entityManager;

    public HikeService(
            HikeRepository hikeRepository,
            UserAccountRepository userAccountRepository,
            GearItemRepository gearItemRepository,
            AssignmentRepository assignmentRepository,
            LoadPlanRepository loadPlanRepository,
            FoodItemRepository foodItemRepository,
            ParticipantRepository participantRepository,
            EntityManager entityManager
    ) {
        this.hikeRepository = hikeRepository;
        this.userAccountRepository = userAccountRepository;
        this.gearItemRepository = gearItemRepository;
        this.assignmentRepository = assignmentRepository;
        this.loadPlanRepository = loadPlanRepository;
        this.foodItemRepository = foodItemRepository;
        this.participantRepository = participantRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public Page<HikeResponse> listForUser(Long userId, Pageable pageable) {
        return hikeRepository.findAccessibleByUser(userId, pageable).map(hike -> {
            hike.getOrganizer().getId();
            hike.getParticipants().size();
            hike.getGearItems().size();
            return HikeResponse.from(hike);
        });
    }

    @Transactional(readOnly = true)
    public Page<HikeResponse> listCopySources(Long currentHikeId, Long userId, Pageable pageable) {
        Hike hike = getAccessibleHike(currentHikeId, userId);
        ensureOrganizer(hike, userId);
        return hikeRepository.findCopySources(userId, currentHikeId, pageable).map(HikeResponse::from);
    }

    @Transactional(readOnly = true)
    public HikeResponse getById(Long hikeId, Long userId) {
        Hike hike = getAccessibleHike(hikeId, userId);
        hike.getParticipants().size();
        hike.getGearItems().size();
        return HikeResponse.from(hike);
    }

    @Transactional
    public HikeResponse create(CreateHikeRequest request, Long organizerUserId) {
        UserAccount organizer = userAccountRepository.findById(organizerUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Hike hike = new Hike();
        hike.setName(request.name().trim());
        hike.setStartDate(request.startDate());
        hike.setDurationDays(request.durationDays());
        hike.setStartLat(request.startLat());
        hike.setStartLon(request.startLon());
        hike.setStatus(HikeStatus.DRAFT);
        hike.setOrganizer(organizer);

        Participant organizerParticipant = new Participant();
        organizerParticipant.setName(organizer.getDisplayName());
        organizerParticipant.setEmail(organizer.getEmail());
        organizerParticipant.setGender(Gender.MALE);
        organizerParticipant.setRole(ParticipantRole.ORGANIZER);
        organizerParticipant.setUser(organizer);
        WeightLimit limit = new WeightLimit();
        limit.setMaxKg(20.0);
        organizerParticipant.setWeightLimit(limit);
        hike.addParticipant(organizerParticipant);

        return HikeResponse.from(hikeRepository.save(hike));
    }

    @Transactional
    public HikeResponse copyGearFromPreviousHike(Long targetHikeId, Long sourceHikeId, Long userId) {
        Hike target = getAccessibleHike(targetHikeId, userId);
        Hike source = getAccessibleHike(sourceHikeId, userId);
        ensureOrganizer(target, userId);

        target.getParticipants().size();

        gearItemRepository.findByHikeIdOrderByNameAsc(source.getId()).forEach(sourceItem -> {
            GearItem copy = new GearItem();
            copy.setName(sourceItem.getName());
            copy.setWeightKg(sourceItem.getWeightKg());
            copy.setHike(target);

            if (sourceItem.isPersonal()) {
                Participant mappedOwner = mapOwnerToTargetHike(sourceItem.getOwner(), target);
                if (mappedOwner != null) {
                    copy.setType(ItemType.PERSONAL);
                    copy.setOwner(mappedOwner);
                } else {
                    copy.setType(ItemType.SHARED);
                }
            } else {
                copy.setType(ItemType.SHARED);
            }

            gearItemRepository.save(copy);
        });

        return HikeResponse.from(hikeRepository.findById(targetHikeId).orElseThrow());
    }

    /**
     * При копировании личного снаряжения ищем того же участника в целевом походе (email или user).
     */
    private Participant mapOwnerToTargetHike(Participant sourceOwner, Hike target) {
        if (sourceOwner == null) {
            return null;
        }
        if (sourceOwner.getEmail() != null && !sourceOwner.getEmail().isBlank()) {
            String email = sourceOwner.getEmail().trim().toLowerCase();
            for (Participant p : target.getParticipants()) {
                if (p.getEmail() != null && email.equals(p.getEmail().trim().toLowerCase())) {
                    return p;
                }
            }
        }
        if (sourceOwner.getUser() != null) {
            Long userId = sourceOwner.getUser().getId();
            for (Participant p : target.getParticipants()) {
                if (p.getUser() != null && userId.equals(p.getUser().getId())) {
                    return p;
                }
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Hike getAccessibleHike(Long hikeId, Long userId) {
        Hike hike = hikeRepository.findById(hikeId)
                .orElseThrow(() -> new NotFoundException("Поход не найден"));
        boolean access = hike.getOrganizer().getId().equals(userId)
                || hike.getParticipants().stream()
                .anyMatch(p -> p.getUser() != null && p.getUser().getId().equals(userId));
        if (!access) {
            throw new NotFoundException("Поход не найден");
        }
        hike.getParticipants().size();
        return hike;
    }

    public void ensureOrganizer(Hike hike, Long userId) {
        boolean isOrganizer = hike.getOrganizer().getId().equals(userId);
        if (!isOrganizer) {
            throw new BusinessException("Только организатор может выполнить это действие");
        }
    }

    @Transactional
    public void delete(Long hikeId, Long userId) {
        Hike hike = getAccessibleHike(hikeId, userId);
        ensureOrganizer(hike, userId);

        assignmentRepository.deleteByHikeId(hikeId);
        loadPlanRepository.deleteByHikeId(hikeId);
        gearItemRepository.deleteByHikeId(hikeId);
        foodItemRepository.deleteByHikeId(hikeId);
        participantRepository.deleteByHikeId(hikeId);

        // Сброс кэша: bulk-delete уже убрал дочерние строки, а в сессии остался Hike с participants
        entityManager.flush();
        entityManager.clear();

        hikeRepository.deleteById(hikeId);
    }
}
