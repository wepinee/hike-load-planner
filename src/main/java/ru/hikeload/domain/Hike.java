package ru.hikeload.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hikes")
public class Hike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HikeStatus status = HikeStatus.DRAFT;

    @Column(name = "start_lat")
    private Double startLat;

    @Column(name = "start_lon")
    private Double startLon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_user_id", nullable = false)
    private UserAccount organizer;

    @OneToMany(mappedBy = "hike", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "hike", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GearItem> gearItems = new ArrayList<>();

    @OneToMany(mappedBy = "hike", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodItem> foodItems = new ArrayList<>();

    @OneToOne(mappedBy = "hike", cascade = CascadeType.ALL, orphanRemoval = true)
    private LoadPlan loadPlan;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public HikeStatus getStatus() {
        return status;
    }

    public void setStatus(HikeStatus status) {
        this.status = status;
    }

    public Double getStartLat() {
        return startLat;
    }

    public void setStartLat(Double startLat) {
        this.startLat = startLat;
    }

    public Double getStartLon() {
        return startLon;
    }

    public void setStartLon(Double startLon) {
        this.startLon = startLon;
    }

    public UserAccount getOrganizer() {
        return organizer;
    }

    public void setOrganizer(UserAccount organizer) {
        this.organizer = organizer;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public List<GearItem> getGearItems() {
        return gearItems;
    }

    public void setGearItems(List<GearItem> gearItems) {
        this.gearItems = gearItems;
    }

    public List<FoodItem> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    public LoadPlan getLoadPlan() {
        return loadPlan;
    }

    public void setLoadPlan(LoadPlan loadPlan) {
        this.loadPlan = loadPlan;
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setHike(this);
    }

    public double calculateTotalGearWeight() {
        return gearItems.stream().mapToDouble(GearItem::getWeightKg).sum();
    }

    public double calculateTotalCarryCapacity() {
        return participants.stream()
                .map(Participant::getWeightLimit)
                .mapToDouble(WeightLimit::getMaxKg)
                .sum();
    }

    public boolean checkDistributionPossibility() {
        return calculateTotalGearWeight() <= calculateTotalCarryCapacity();
    }
}
