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
@Table(name = "gear_items")
public class GearItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hike_id", nullable = false)
    private Hike hike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_participant_id")
    private Participant owner;

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

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public Hike getHike() {
        return hike;
    }

    public void setHike(Hike hike) {
        this.hike = hike;
    }

    public Participant getOwner() {
        return owner;
    }

    public void setOwner(Participant owner) {
        this.owner = owner;
    }

    public boolean isShared() {
        return type == ItemType.SHARED;
    }

    public boolean isPersonal() {
        return type == ItemType.PERSONAL;
    }

    public double calculateSharePerPerson(int participantCount) {
        if (!isShared() || participantCount <= 0) {
            return weightKg;
        }
        return weightKg / participantCount;
    }
}
