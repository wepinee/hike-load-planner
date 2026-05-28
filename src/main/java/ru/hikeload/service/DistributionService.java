package ru.hikeload.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hikeload.domain.Assignment;
import ru.hikeload.domain.AssignmentType;
import ru.hikeload.domain.FoodItem;
import ru.hikeload.domain.GearItem;
import ru.hikeload.domain.Hike;
import ru.hikeload.domain.LoadPlan;
import ru.hikeload.domain.Participant;
import ru.hikeload.repository.AssignmentRepository;
import ru.hikeload.repository.FoodItemRepository;
import ru.hikeload.repository.GearItemRepository;
import ru.hikeload.repository.LoadPlanRepository;
import ru.hikeload.repository.ParticipantRepository;
import ru.hikeload.service.distribution.DistributionContext;
import ru.hikeload.service.distribution.DistributionValidator;
import ru.hikeload.service.distribution.LoadDistributionStrategy;
import ru.hikeload.web.dto.LoadPlanExportResponse;
import ru.hikeload.web.dto.LoadPlanResponse;
import ru.hikeload.web.dto.ReassignItemRequest;

import java.util.List;

@Service
public class DistributionService {

    private final LoadPlanRepository loadPlanRepository;
    private final GearItemRepository gearItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final AssignmentRepository assignmentRepository;
    private final ParticipantRepository participantRepository;
    private final HikeService hikeService;
    private final DistributionValidator distributionValidator;
    private final List<LoadDistributionStrategy> distributionStrategies;
    private final LoadPlanPdfExporter loadPlanPdfExporter;

    public DistributionService(
            LoadPlanRepository loadPlanRepository,
            GearItemRepository gearItemRepository,
            FoodItemRepository foodItemRepository,
            AssignmentRepository assignmentRepository,
            ParticipantRepository participantRepository,
            HikeService hikeService,
            DistributionValidator distributionValidator,
            List<LoadDistributionStrategy> distributionStrategies,
            LoadPlanPdfExporter loadPlanPdfExporter
    ) {
        this.loadPlanRepository = loadPlanRepository;
        this.gearItemRepository = gearItemRepository;
        this.foodItemRepository = foodItemRepository;
        this.assignmentRepository = assignmentRepository;
        this.participantRepository = participantRepository;
        this.hikeService = hikeService;
        this.distributionValidator = distributionValidator;
        this.distributionStrategies = distributionStrategies;
        this.loadPlanPdfExporter = loadPlanPdfExporter;
    }

    @Transactional
    public LoadPlanResponse generate(Long hikeId, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        hikeService.ensureOrganizer(hike, userId);

        List<GearItem> gearItems = gearItemRepository.findByHikeIdOrderByNameAsc(hikeId);
        List<FoodItem> foodItems = foodItemRepository.findByHikeIdOrderByNameAsc(hikeId);

        LoadPlan plan = loadPlanRepository.findByHikeId(hikeId).orElseGet(LoadPlan::new);
        plan.setHike(hike);
        hike.setLoadPlan(plan);
        plan.getAssignments().clear();
        int currentVersion = plan.getVersion() == null ? 0 : plan.getVersion();
        plan.setVersion(currentVersion + 1);

        DistributionContext context = new DistributionContext(hike, plan, gearItems, foodItems);
        distributionValidator.validate(context);
        distributionStrategies.forEach(strategy -> strategy.distribute(context));

        LoadPlan saved = loadPlanRepository.save(plan);
        hike.getParticipants().size();
        return LoadPlanResponse.from(saved, hike);
    }

    @Transactional
    public LoadPlanResponse reassign(Long hikeId, ReassignItemRequest request, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        hikeService.ensureOrganizer(hike, userId);

        Assignment assignment = assignmentRepository.findById(request.assignmentId())
                .orElseThrow(() -> new NotFoundException("Назначение не найдено"));
        if (!assignment.getLoadPlan().getHike().getId().equals(hikeId)) {
            throw new NotFoundException("Назначение не найдено");
        }
        if (assignment.getAssignmentType() == AssignmentType.GEAR_PERSONAL) {
            throw new BusinessException("Личное снаряжение нельзя переназначить другому участнику");
        }
        if (assignment.getAssignmentType() == AssignmentType.FOOD) {
            throw new BusinessException("Порции питания закреплены за каждым участником");
        }

        Participant to = participantRepository.findByIdAndHikeId(request.toParticipantId(), hikeId)
                .orElseThrow(() -> new NotFoundException("Участник не найден"));

        int n = hike.getParticipants().size();
        assignment.setParticipant(to);
        assignment.recalculateWeight(n, hike.getDurationDays());
        assignmentRepository.save(assignment);

        LoadPlan plan = assignment.getLoadPlan();
        Hike hikeEntity = hikeService.getAccessibleHike(hikeId, userId);
        return LoadPlanResponse.from(plan, hikeEntity);
    }

    @Transactional(readOnly = true)
    public LoadPlanResponse get(Long hikeId, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        return getPlanForHike(hike);
    }

    @Transactional(readOnly = true)
    public LoadPlanResponse getMyLoad(Long hikeId, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        LoadPlanResponse full = getPlanForHike(hike);
        Participant me = hike.getParticipants().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Вы не являетесь участником этого похода"));

        List<LoadPlanResponse.AssignmentLine> mine = full.assignments().stream()
                .filter(a -> a.participantId().equals(me.getId()))
                .toList();
        List<LoadPlanResponse.ParticipantLoadSummary> summary = full.summary().stream()
                .filter(s -> s.participantId().equals(me.getId()))
                .toList();

        return new LoadPlanResponse(full.id(), full.createdAt(), full.version(), mine, summary);
    }

    @Transactional(readOnly = true)
    public LoadPlanExportResponse export(Long hikeId, Long userId) {
        Hike hike = hikeService.getAccessibleHike(hikeId, userId);
        LoadPlanResponse plan = getPlanForHike(hike);
        return LoadPlanExportResponse.from(hike, plan);
    }

    @Transactional(readOnly = true)
    public byte[] exportPdf(Long hikeId, Long userId) {
        LoadPlanExportResponse data = export(hikeId, userId);
        return loadPlanPdfExporter.export(data);
    }

    private LoadPlanResponse getPlanForHike(Hike hike) {
        LoadPlan plan = loadPlanRepository.findDetailedByHikeId(hike.getId())
                .orElseThrow(() -> new NotFoundException("Раскладка ещё не сформирована"));
        hike.getParticipants().size();
        return LoadPlanResponse.from(plan, hike);
    }
}
