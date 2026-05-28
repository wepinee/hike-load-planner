package ru.hikeload.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "load_plans")
public class LoadPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private Integer version = 1;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hike_id", nullable = false, unique = true)
    private Hike hike;

    @OneToMany(mappedBy = "loadPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Hike getHike() {
        return hike;
    }

    public void setHike(Hike hike) {
        this.hike = hike;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        assignment.setLoadPlan(this);
    }

    public double getTotalWeightForParticipant(Participant participant) {
        return assignments.stream()
                .filter(a -> a.getParticipant().getId().equals(participant.getId()))
                .mapToDouble(Assignment::getEffectiveWeight)
                .sum();
    }
}
