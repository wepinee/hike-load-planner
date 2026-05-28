package ru.hikeload.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "weight_per_portion_kg", nullable = false)
    private Double weightPerPortionKg;

    @Column(name = "calories_per_portion")
    private Integer caloriesPerPortion;

    @Column(name = "portions_per_person_per_day", nullable = false)
    private Double portionsPerPersonPerDay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hike_id", nullable = false)
    private Hike hike;

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

    public Double getWeightPerPortionKg() {
        return weightPerPortionKg;
    }

    public void setWeightPerPortionKg(Double weightPerPortionKg) {
        this.weightPerPortionKg = weightPerPortionKg;
    }

    public Integer getCaloriesPerPortion() {
        return caloriesPerPortion;
    }

    public void setCaloriesPerPortion(Integer caloriesPerPortion) {
        this.caloriesPerPortion = caloriesPerPortion;
    }

    public Double getPortionsPerPersonPerDay() {
        return portionsPerPersonPerDay;
    }

    public void setPortionsPerPersonPerDay(Double portionsPerPersonPerDay) {
        this.portionsPerPersonPerDay = portionsPerPersonPerDay;
    }

    public Hike getHike() {
        return hike;
    }

    public void setHike(Hike hike) {
        this.hike = hike;
    }

    public double calculateTotalWeight(int days, int personCount) {
        return weightPerPortionKg * portionsPerPersonPerDay * days * personCount;
    }

    public int calculateTotalCalories(int days, int personCount) {
        if (caloriesPerPortion == null) {
            return 0;
        }
        return (int) (caloriesPerPortion * portionsPerPersonPerDay * days * personCount);
    }
}
