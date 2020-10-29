package org.demo.project.orchestrator.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReserveCreditDto {
    private Long customerId;
    private String creditReservation;
}
