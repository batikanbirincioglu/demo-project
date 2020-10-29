package org.demo.project.customer.service.dto;

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
