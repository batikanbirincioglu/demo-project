package org.demo.project.customer.service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CreateCustomerDto {
    private String name;
    private String creditLimit;
}
