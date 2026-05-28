package ru.hikeload.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@Table(name = "participants")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    @Embedded
    private WeightLimit weightLimit = new WeightLimit();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hike_id", nullable = false)
    private Hike hike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public void setRole(ParticipantRole role) {
        this.role = role;
    }

    public WeightLimit getWeightLimit() {
        return weightLimit;
    }

    public void setWeightLimit(WeightLimit weightLimit) {
        this.weightLimit = weightLimit;
    }

    public Hike getHike() {
        return hike;
    }

    public void setHike(Hike hike) {
        this.hike = hike;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public double calculateCurrentLoad(LoadPlan loadPlan) {
        if (loadPlan == null) {
            return 0;
        }
        return loadPlan.getAssignments().stream()
                .filter(a -> a.getParticipant().getId().equals(id))
                .mapToDouble(Assignment::getEffectiveWeight)
                .sum();
    }

    public boolean canCarry(GearItem item, int participantCount, double currentLoad) {
        double additional = item.isShared()
                ? item.calculateSharePerPerson(participantCount)
                : item.getWeightKg();
        return weightLimit.isWithinLimit(currentLoad + additional);
    }
}
