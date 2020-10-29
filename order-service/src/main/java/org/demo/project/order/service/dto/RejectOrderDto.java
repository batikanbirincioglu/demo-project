package org.demo.project.order.service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RejectOrderDto {
    private Long orderId;
    private String rejectionReason;
}