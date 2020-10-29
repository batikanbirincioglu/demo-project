package org.demo.project.orchestrator.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CreateOrderDto {
    private Long customerId;
    private String amount;
}
