package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.dto.MedicineSearchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PharmacyStockRepository extends JpaRepository<PharmacyStock, String> {

    // ==========================================
    // 1. INVENTORY & RESERVATION METHODS (Dev 2 & 3)
    // ==========================================

    List<PharmacyStock> findByPharmacyId(String pharmacyId);

    List<PharmacyStock> findAllByPharmacyIdAndMedicineId(String pharmacyId, String medicineId);

    @Query("SELECT ps FROM PharmacyStock ps WHERE ps.medicine.id = :medicineId AND ps.quantity > 0")
    List<PharmacyStock> findAvailableStockByMedicine(@Param("medicineId") String medicineId);

    @Query("SELECT ps FROM PharmacyStock ps " +
            "JOIN FETCH ps.pharmacy p " +
            "WHERE ps.medicine.id = :medicineId " +
            "AND ps.quantity > 0 " +
            "AND p.isVerified = true " +
            "AND p.isActive = true " +
            "ORDER BY ps.price ASC")
    List<PharmacyStock> findPharmaciesOrderByPriceAsc(@Param("medicineId") String medicineId);


    // ==========================================
    // 2. SEARCH EPIC METHODS (Dev 1)
    // ==========================================

    // MEDICINE FILTER & PRICE COMPARISON (By Keyword/Generic Name)
    @Query("SELECT s FROM PharmacyStock s " +
            "JOIN FETCH s.medicine m " +
            "JOIN FETCH s.pharmacy p " +
            "WHERE (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND s.quantity > 0 AND p.isActive = true AND p.isVerified = true " +
            "ORDER BY s.price ASC")
    List<PharmacyStock> findAvailableMedicineByKeyword(@Param("keyword") String keyword);

    // SPATIAL SEARCH (Native SQL with Haversine Formula for GPS distance)
    @Query(value =
            "SELECT ps.id as stockId, p.id as pharmacyId, m.id as medicineId, " +
                    "p.name as pharmacyName, p.address as pharmacyAddress, " +
                    "p.lat as lat, p.lng as lng, m.name as medicineName, m.generic_name as genericName, " +
                    "ps.quantity as quantity, ps.price as price, " +
                    "( 6371 * acos( cos( radians(:userLat) ) * cos( radians( p.lat ) ) * " +
                    "cos( radians( p.lng ) - radians(:userLng) ) + sin( radians(:userLat) ) * " +
                    "sin( radians( p.lat ) ) ) ) AS distance " +
                    "FROM pharmacy_stock ps " +
                    "JOIN pharmacy p ON ps.pharmacy_id = p.id " +
                    "JOIN medicine m ON ps.medicine_id = m.id " +
                    "WHERE (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(m.generic_name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "AND ps.quantity > 0 AND p.is_active = true AND p.is_verified = true " +
                    "ORDER BY distance ASC",
            nativeQuery = true)
    List<MedicineSearchResult> findClosestPharmaciesWithStock(
            @Param("keyword") String keyword,
            @Param("userLat") Float userLat,
            @Param("userLng") Float userLng);

    @Query(value =
            "SELECT ps.id as stockId, p.id as pharmacyId, m.id as medicineId, " +
                    "p.name as pharmacyName, p.address as pharmacyAddress, " +
                    "p.lat as lat, p.lng as lng, m.name as medicineName, m.generic_name as genericName, " +
                    "ps.quantity as quantity, ps.price as price, " +
                    "( 6371 * acos( cos( radians(:userLat) ) * cos( radians( p.lat ) ) * " +
                    "cos( radians( p.lng ) - radians(:userLng) ) + sin( radians(:userLat) ) * " +
                    "sin( radians( p.lat ) ) ) ) AS distance " +
                    "FROM pharmacy_stock ps " +
                    "JOIN pharmacy p ON ps.pharmacy_id = p.id " +
                    "JOIN medicine m ON ps.medicine_id = m.id " +
                    "WHERE (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(m.generic_name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "AND ps.quantity > 0 AND p.is_active = true AND p.is_verified = true " +
                    "HAVING distance <= :radius " +
                    "ORDER BY distance ASC",
            nativeQuery = true)
    List<MedicineSearchResult> filterNearbyMedicines(
            @Param("keyword") String keyword,
            @Param("userLat") Float userLat,
            @Param("userLng") Float userLng,
            @Param("radius") Double radius);

    // EMERGENCY MODE: 24/7 pharmacies only, sorted strictly by distance
    @Query(value =
            "SELECT ps.id as stockId, p.id as pharmacyId, m.id as medicineId, " +
                    "p.name as pharmacyName, p.address as pharmacyAddress, " +
                    "p.lat as lat, p.lng as lng, m.name as medicineName, m.generic_name as genericName, " +
                    "ps.quantity as quantity, ps.price as price, " +
                    "( 6371 * acos( cos( radians(:userLat) ) * cos( radians( p.lat ) ) * " +
                    "cos( radians( p.lng ) - radians(:userLng) ) + sin( radians(:userLat) ) * " +
                    "sin( radians( p.lat ) ) ) ) AS distance " +
                    "FROM pharmacy_stock ps " +
                    "JOIN pharmacy p ON ps.pharmacy_id = p.id " +
                    "JOIN medicine m ON ps.medicine_id = m.id " +
                    "WHERE (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(m.generic_name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "AND ps.quantity > 0 AND p.is_active = true AND p.is_verified = true AND p.is_24_7 = true " +
                    "ORDER BY distance ASC",
            nativeQuery = true)
    List<MedicineSearchResult> findEmergencyPharmaciesWithStock(
            @Param("keyword") String keyword,
            @Param("userLat") Float userLat,
            @Param("userLng") Float userLng);
}