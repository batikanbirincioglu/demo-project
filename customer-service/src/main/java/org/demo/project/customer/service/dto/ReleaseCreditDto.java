package org.demo.project.customer.service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ReleaseCreditDto {
    private Long customerId;
    private String creditRelease;
}
