package org.demo.project.order.service.dto;

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
