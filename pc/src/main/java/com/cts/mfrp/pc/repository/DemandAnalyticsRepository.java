package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.dto.MedicineDemandView;
import com.cts.mfrp.pc.model.DemandAnalytics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandAnalyticsRepository extends JpaRepository<DemandAnalytics, String> {

    Optional<DemandAnalytics> findByPharmacyIdAndMedicineIdAndPeriodDate(String pharmacyId, String medicineId, LocalDate periodDate);

    // US-15: Top-demand medicines for a pharmacy, aggregated per medicine across the given
    // time window (one row per medicine, most-searched first). Use a Pageable to cap at top-N.
    @Query("SELECT m.id AS medicineId, m.name AS medicineName, m.genericName AS genericName, " +
           "SUM(d.searchCount) AS totalSearches, SUM(d.reservationCount) AS totalReservations, " +
           "MAX(d.periodDate) AS lastActivity " +
           "FROM DemandAnalytics d JOIN d.medicine m " +
           "WHERE d.pharmacy.id = :pharmacyId AND d.periodDate >= :since " +
           "GROUP BY m.id, m.name, m.genericName " +
           "ORDER BY SUM(d.searchCount) DESC, SUM(d.reservationCount) DESC")
    List<MedicineDemandView> findTopDemandByPharmacy(@Param("pharmacyId") String pharmacyId,
                                                     @Param("since") LocalDate since,
                                                     Pageable pageable);
}
