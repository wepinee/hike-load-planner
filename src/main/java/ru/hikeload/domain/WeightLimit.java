package ru.hikeload.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class WeightLimit {

    @Column(name = "max_kg", nullable = false)
    private Double maxKg;

    public Double getMaxKg() {
        return maxKg;
    }

    public void setMaxKg(Double maxKg) {
        this.maxKg = maxKg;
    }

    public boolean isWithinLimit(double weight) {
        return weight <= maxKg;
    }

    public double getRemainingCapacity(double currentLoad) {
        return Math.max(0, maxKg - currentLoad);
    }
}
