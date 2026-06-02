package com.cts.mfrp.pc.dto;

import java.time.LocalDate;

/**
 * Spring Data projection for aggregated per-medicine demand at a pharmacy.
 * Totals are summed across the requested time window (one row per medicine).
 */
public interface MedicineDemandView {
    String getMedicineId();
    String getMedicineName();
    String getGenericName();
    Long getTotalSearches();
    Long getTotalReservations();
    LocalDate getLastActivity();
}
