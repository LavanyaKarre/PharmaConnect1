package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.DemandAnalyticsResponseDto;
import com.cts.mfrp.pc.model.DemandAnalytics;
import com.cts.mfrp.pc.model.Medicine;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.repository.DemandAnalyticsRepository;
import com.cts.mfrp.pc.repository.MedicineRepository;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandAnalyticsService {

    private final DemandAnalyticsRepository analyticsRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;

    // US-14: Called after a search result is returned — increments search count
    @Transactional
    public void recordSearch(String pharmacyId, String medicineId) {
        LocalDate today = LocalDate.now();
        DemandAnalytics record = analyticsRepository
                .findByPharmacyIdAndMedicineIdAndPeriodDate(pharmacyId, medicineId, today)
                .orElseGet(() -> createNewRecord(pharmacyId, medicineId, today));

        record.setSearchCount(record.getSearchCount() + 1);
        analyticsRepository.save(record);
    }

    // US-14: Called when a reservation is created — increments reservation count
    @Transactional
    public void recordReservation(String pharmacyId, String medicineId) {
        LocalDate today = LocalDate.now();
        DemandAnalytics record = analyticsRepository
                .findByPharmacyIdAndMedicineIdAndPeriodDate(pharmacyId, medicineId, today)
                .orElseGet(() -> createNewRecord(pharmacyId, medicineId, today));

        record.setReservationCount(record.getReservationCount() + 1);
        analyticsRepository.save(record);
    }

    // US-15: Seller views analytics for their pharmacy.
    // Returns the top 10 most-demanded medicines over the last 30 days, aggregated
    // to one row per medicine (instead of one raw row per medicine per day).
    private static final int TOP_N = 10;
    private static final int WINDOW_DAYS = 30;

    public List<DemandAnalyticsResponseDto> getPharmacyAnalytics(String pharmacyId) {
        LocalDate since = LocalDate.now().minusDays(WINDOW_DAYS);
        return analyticsRepository
                .findTopDemandByPharmacy(pharmacyId, since, PageRequest.of(0, TOP_N))
                .stream()
                .map(v -> DemandAnalyticsResponseDto.builder()
                        .medicineId(v.getMedicineId())
                        .medicineName(v.getMedicineName())
                        .genericName(v.getGenericName())
                        .searchCount(v.getTotalSearches())
                        .reservationCount(v.getTotalReservations())
                        .periodDate(v.getLastActivity())
                        .build())
                .collect(Collectors.toList());
    }

    private DemandAnalytics createNewRecord(String pharmacyId, String medicineId, LocalDate date) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId).orElseThrow();
        Medicine medicine = medicineRepository.findById(medicineId).orElseThrow();

        DemandAnalytics record = new DemandAnalytics();
        record.setPharmacy(pharmacy);
        record.setMedicine(medicine);
        record.setSearchCount(0);
        record.setReservationCount(0);
        record.setPeriodDate(date);
        return record;
    }
}
