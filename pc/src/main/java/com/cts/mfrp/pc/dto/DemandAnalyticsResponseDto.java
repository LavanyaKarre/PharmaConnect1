package com.cts.mfrp.pc.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DemandAnalyticsResponseDto {
    private String medicineId;
    private String medicineName;
    private String genericName;
    private Long searchCount;        // total search appearances in the window
    private Long reservationCount;   // total reservations in the window
    private LocalDate periodDate;    // most recent day this medicine saw activity
}
