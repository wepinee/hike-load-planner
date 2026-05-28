package ru.hikeload.service.distribution;

/**
 * Strategy: алгоритм распределения одной категории нагрузки.
 */
public interface LoadDistributionStrategy {

    void distribute(DistributionContext context);
}
