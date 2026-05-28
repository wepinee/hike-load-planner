package ru.hikeload.web.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.hikeload.domain.Gender;
import ru.hikeload.domain.Hike;
import ru.hikeload.domain.ItemType;
import ru.hikeload.security.CurrentUserService;
import ru.hikeload.service.BusinessException;
import ru.hikeload.service.DistributionService;
import ru.hikeload.service.FoodService;
import ru.hikeload.service.GearService;
import ru.hikeload.service.HikeService;
import ru.hikeload.service.NotFoundException;
import ru.hikeload.service.ParticipantService;
import ru.hikeload.web.dto.AddFoodItemRequest;
import ru.hikeload.web.dto.AddGearItemRequest;
import ru.hikeload.web.dto.AddParticipantRequest;
import ru.hikeload.web.dto.CreateHikeRequest;
import ru.hikeload.web.form.HikeCreateForm;
import ru.hikeload.web.form.ParticipantForm;

@Controller
@RequestMapping("/hikes")
public class HikePageController {

    private final HikeService hikeService;
    private final ParticipantService participantService;
    private final GearService gearService;
    private final FoodService foodService;
    private final DistributionService distributionService;
    private final CurrentUserService currentUserService;

    public HikePageController(
            HikeService hikeService,
            ParticipantService participantService,
            GearService gearService,
            FoodService foodService,
            DistributionService distributionService,
            CurrentUserService currentUserService
    ) {
        this.hikeService = hikeService;
        this.participantService = participantService;
        this.gearService = gearService;
        this.foodService = foodService;
        this.distributionService = distributionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String list(Model model) {
        Long userId = currentUserService.getCurrentUserId();
        var hikes = hikeService.listForUser(userId, PageRequest.of(0, 50, Sort.by("startDate").descending()));
        model.addAttribute("hikes", hikes.getContent());
        model.addAttribute("hikeForm", new HikeCreateForm());
        model.addAttribute("currentUserId", userId);
        model.addAttribute("userName", currentUserService.getCurrentUser().getDisplayName());
        return "hikes/list";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("hikeForm") HikeCreateForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            Long userId = currentUserService.getCurrentUserId();
            model.addAttribute("hikes", hikeService.listForUser(userId,
                    PageRequest.of(0, 50, Sort.by("startDate").descending())).getContent());
            model.addAttribute("userName", currentUserService.getCurrentUser().getDisplayName());
            return "hikes/list";
        }
        var created = hikeService.create(
                new CreateHikeRequest(form.getName(), form.getStartDate(), form.getDurationDays(), null, null),
                currentUserService.getCurrentUserId()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Поход создан");
        return "redirect:/hikes/" + created.id();
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "gear") String tab,
            Model model
    ) {
        Long userId = currentUserService.getCurrentUserId();
        Hike hike = hikeService.getAccessibleHike(id, userId);
        boolean organizer = hike.getOrganizer().getId().equals(userId);
        Long myParticipantId = hike.getParticipants().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                .map(p -> p.getId())
                .findFirst()
                .orElse(null);

        model.addAttribute("hike", hikeService.getById(id, userId));
        model.addAttribute("hikeEntity", hike);
        model.addAttribute("organizer", organizer);
        model.addAttribute("myParticipantId", myParticipantId);
        model.addAttribute("activeTab", tab);
        model.addAttribute("participants", participantService.listByHike(id, userId));
        model.addAttribute("gear", gearService.listByHike(id, userId));
        model.addAttribute("food", foodService.listByHike(id, userId));
        model.addAttribute("participantForm", new ParticipantForm());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("itemTypes", ItemType.values());

        if (organizer) {
            model.addAttribute("copySources", hikeService.listCopySources(id, userId,
                    PageRequest.of(0, 20, Sort.by("startDate").descending())).getContent());
        }

        try {
            model.addAttribute("loadPlan", distributionService.get(id, userId));
        } catch (NotFoundException ignored) {
            model.addAttribute("loadPlan", null);
        }

        try {
            model.addAttribute("myLoadPlan", distributionService.getMyLoad(id, userId));
        } catch (BusinessException | NotFoundException ignored) {
            model.addAttribute("myLoadPlan", null);
        }

        return "hikes/detail";
    }

    @PostMapping("/{id}/participants")
    public String addParticipant(
            @PathVariable Long id,
            @Valid @ModelAttribute ParticipantForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Проверьте данные участника");
            return "redirect:/hikes/" + id + "?tab=participants";
        }
        participantService.add(id, new AddParticipantRequest(
                form.getName(), form.getEmail(), form.getGender(), form.getMaxWeightKg(), null
        ), currentUserService.getCurrentUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Участник добавлен");
        return "redirect:/hikes/" + id + "?tab=participants";
    }

    @PostMapping("/{id}/participants/{participantId}/delete")
    public String deleteParticipant(
            @PathVariable Long id,
            @PathVariable Long participantId,
            RedirectAttributes redirectAttributes
    ) {
        participantService.remove(id, participantId, currentUserService.getCurrentUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Участник удалён");
        return "redirect:/hikes/" + id + "?tab=participants";
    }

    @PostMapping("/{id}/gear/shared")
    public String addSharedGear(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam Double weightKg,
            RedirectAttributes redirectAttributes
    ) {
        gearService.addShared(id, new AddGearItemRequest(name, weightKg, ItemType.SHARED, null),
                currentUserService.getCurrentUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Предмет добавлен");
        return "redirect:/hikes/" + id + "?tab=gear";
    }

    @PostMapping("/{id}/gear/{gearItemId}/update")
    public String updateGear(
            @PathVariable Long id,
            @PathVariable Long gearItemId,
            @RequestParam String name,
            @RequestParam Double weightKg,
            RedirectAttributes redirectAttributes
    ) {
        try {
            gearService.update(id, gearItemId,
                    new ru.hikeload.web.dto.UpdateGearItemRequest(name, weightKg),
                    currentUserService.getCurrentUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Снаряжение обновлено");
        } catch (BusinessException | NotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/hikes/" + id + "?tab=gear";
    }

    @PostMapping("/{id}/food/{foodItemId}/update")
    public String updateFood(
            @PathVariable Long id,
            @PathVariable Long foodItemId,
            @RequestParam String name,
            @RequestParam Double weightPerPortionKg,
            @RequestParam(required = false) Integer caloriesPerPortion,
            @RequestParam Double portionsPerPersonPerDay,
            RedirectAttributes redirectAttributes
    ) {
        try {
            foodService.update(id, foodItemId, new ru.hikeload.web.dto.UpdateFoodItemRequest(
                    name, weightPerPortionKg, caloriesPerPortion, portionsPerPersonPerDay
            ), currentUserService.getCurrentUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Продукт обновлён");
        } catch (BusinessException | NotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/hikes/" + id + "?tab=food";
    }

    @PostMapping("/{id}/gear/{gearItemId}/delete")
    public String deleteGear(
            @PathVariable Long id,
            @PathVariable Long gearItemId,
            RedirectAttributes redirectAttributes
    ) {
        gearService.delete(id, gearItemId, currentUserService.getCurrentUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Предмет удалён");
        return "redirect:/hikes/" + id + "?tab=gear";
    }

    @PostMapping("/{id}/food")
    public String addFood(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam Double weightPerPortionKg,
            @RequestParam(required = false) Integer caloriesPerPortion,
            @RequestParam Double portionsPerPersonPerDay,
            RedirectAttributes redirectAttributes
    ) {
        foodService.add(id, new AddFoodItemRequest(
                name, weightPerPortionKg, caloriesPerPortion, portionsPerPersonPerDay
        ), currentUserService.getCurrentUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Продукт добавлен");
        return "redirect:/hikes/" + id + "?tab=food";
    }

    @PostMapping("/{id}/food/{foodItemId}/delete")
    public String deleteFood(
            @PathVariable Long id,
            @PathVariable Long foodItemId,
            RedirectAttributes redirectAttributes
    ) {
        foodService.delete(id, foodItemId, currentUserService.getCurrentUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Продукт удалён");
        return "redirect:/hikes/" + id + "?tab=food";
    }

    @PostMapping("/{id}/load-plan/generate")
    public String generateLoadPlan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            distributionService.generate(id, currentUserService.getCurrentUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Раскладка сформирована");
        } catch (BusinessException | NotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при формировании раскладки: " + ex.getMessage());
        }
        return "redirect:/hikes/" + id + "?tab=loadplan";
    }

    @PostMapping("/{id}/gear/copy-from")
    public String copyGear(
            @PathVariable Long id,
            @RequestParam Long sourceHikeId,
            RedirectAttributes redirectAttributes
    ) {
        hikeService.copyGearFromPreviousHike(id, sourceHikeId, currentUserService.getCurrentUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Снаряжение скопировано");
        return "redirect:/hikes/" + id + "?tab=gear";
    }

    @PostMapping("/{id}/delete")
    public String deleteHike(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            hikeService.delete(id, currentUserService.getCurrentUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Поход удалён");
            return "redirect:/hikes";
        } catch (BusinessException | NotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/hikes/" + id;
        }
    }
}
