package ru.hikeload.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hikeload.domain.GearItem;
import ru.hikeload.domain.Hike;
import ru.hikeload.domain.ItemType;
import ru.hikeload.domain.Participant;
import ru.hikeload.repository.AssignmentRepository;
import ru.hikeload.repository.GearItemRepository;
import ru.hikeload.repository.ParticipantRepository;
import ru.hikeload.web.dto.AddGearItemRequest;
import ru.hikeload.web.dto.GearItemResponse;
import ru.hikeload.web.dto.UpdateGearItemRequest;

import java.util.List;

@Service
public class GearService {

    private final GearItemRepository gearItemRepository;
    private final AssignmentRepository assignmentRepository;
    private final ParticipantRepository participantRepository;
    private final HikeService hikeService;

    public GearService(
            GearItemRepository gearItemRepository,
            AssignmentRepository assignmentRepository,
            ParticipantRepository participantRepository,
            HikeService hikeService
    ) {
        this.gearItemRepository = gearItemRepository;
        this.assignmentRepository = assignmentRepository;
        this.participantRepository = participantRepository;
        this.hikeService = hikeService;
    }

    @Transactional(readOnly = true)
    public List<GearItemResponse> listByHike(Long hikeId, Long userId) {
        hikeService.getAccessibleHike(hikeId, userId);
        return gearItemRepository.findByHikeIdOrderByNameAsc(hikeId).stream()
                .map(GearItemResponse::from)
                .toList();
    }

    @Transactional
    public GearItemResponse addShared(Long hikeId, AddGearItemRequest request, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        hikeService.ensureOrganizer(hike, userId);
        if (request.type() != ItemType.SHARED) {
            throw new BusinessException("Для этого метода допустимо только общее снаряжение");
        }
        return GearItemResponse.from(saveItem(hike, request, null));
    }

    @Transactional
    public GearItemResponse addPersonal(Long hikeId, AddGearItemRequest request, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        if (request.type() != ItemType.PERSONAL) {
            throw new BusinessException("Для личного снаряжения укажите тип PERSONAL");
        }

        Participant owner = resolveOwner(hike, request.ownerParticipantId(), userId);

        return GearItemResponse.from(saveItem(hike, request, owner));
    }

    @Transactional
    public void delete(Long hikeId, Long gearItemId, Long userId) {
        GearItem item = gearItemRepository.findById(gearItemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));
        if (!item.getHike().getId().equals(hikeId)) {
            throw new NotFoundException("Предмет не найден");
        }
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        if (item.isPersonal()) {
            assertPersonalOwner(item, userId);
        } else {
            hikeService.ensureOrganizer(hike, userId);
        }
        assignmentRepository.deleteByGearItemId(gearItemId);
        gearItemRepository.delete(item);
    }

    @Transactional
    public GearItemResponse update(Long hikeId, Long gearItemId, UpdateGearItemRequest request, Long userId) {
        GearItem item = gearItemRepository.findById(gearItemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));
        if (!item.getHike().getId().equals(hikeId)) {
            throw new NotFoundException("Предмет не найден");
        }
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        if (item.isPersonal()) {
            assertPersonalOwner(item, userId);
        } else {
            hikeService.ensureOrganizer(hike, userId);
        }
        item.setName(request.name().trim());
        item.setWeightKg(request.weightKg());
        return GearItemResponse.from(gearItemRepository.save(item));
    }

    @Transactional
    public GearItemResponse updateWeight(Long gearItemId, Double weightKg, Long userId) {
        GearItem item = gearItemRepository.findById(gearItemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));
        return update(item.getHike().getId(), gearItemId,
                new UpdateGearItemRequest(item.getName(), weightKg), userId);
    }

    private GearItem saveItem(Hike hike, AddGearItemRequest request, Participant owner) {
        GearItem item = new GearItem();
        item.setName(request.name().trim());
        item.setWeightKg(request.weightKg());
        item.setType(request.type());
        item.setHike(hike);
        item.setOwner(owner);
        return gearItemRepository.save(item);
    }

    private Participant resolveOwner(Hike hike, Long ownerParticipantId, Long userId) {
        if (ownerParticipantId == null) {
            return hike.getParticipants().stream()
                    .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Укажите участника-владельца личного предмета"));
        }
        return participantRepository.findByIdAndHikeId(ownerParticipantId, hike.getId())
                .orElseThrow(() -> new NotFoundException("Участник не найден"));
    }

    private void assertPersonalOwner(GearItem item, Long userId) {
        if (item.getOwner() == null
                || item.getOwner().getUser() == null
                || !item.getOwner().getUser().getId().equals(userId)) {
            hikeService.ensureOrganizer(item.getHike(), userId);
        }
    }
}
