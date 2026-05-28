package ru.hikeload.service.distribution;

/**
 * Specification / Policy: проверка условий перед или при формировании раскладки.
 */
public interface DistributionRule {

    void check(DistributionContext context);
}
