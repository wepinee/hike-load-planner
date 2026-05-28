package ru.hikeload.domain;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false)
    private AssignmentType assignmentType;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "weight_share", nullable = false)
    private Double weightShare;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "load_plan_id", nullable = false)
    private LoadPlan loadPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "gear_item_id", nullable = true)
    private GearItem gearItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "food_item_id", nullable = true)
    private FoodItem foodItem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getWeightShare() {
        return weightShare;
    }

    public void setWeightShare(Double weightShare) {
        this.weightShare = weightShare;
    }

    public LoadPlan getLoadPlan() {
        return loadPlan;
    }

    public void setLoadPlan(LoadPlan loadPlan) {
        this.loadPlan = loadPlan;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public GearItem getGearItem() {
        return gearItem;
    }

    public void setGearItem(GearItem gearItem) {
        this.gearItem = gearItem;
    }

    public FoodItem getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
    }

    public void recalculateWeight(int participantCount, int hikeDays) {
        if (assignmentType == AssignmentType.FOOD && foodItem != null) {
            weightShare = foodItem.calculateTotalWeight(hikeDays, participantCount) / participantCount;
            return;
        }
        if (gearItem == null) {
            weightShare = 0.0;
            return;
        }
        if (assignmentType == AssignmentType.GEAR_SHARED) {
            weightShare = gearItem.calculateSharePerPerson(participantCount) * quantity;
        } else {
            weightShare = gearItem.getWeightKg() * quantity;
        }
    }

    public double getEffectiveWeight() {
        return weightShare != null ? weightShare : 0;
    }
}
