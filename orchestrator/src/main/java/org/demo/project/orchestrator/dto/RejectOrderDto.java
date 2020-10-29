package org.demo.project.orchestrator.dto;

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
