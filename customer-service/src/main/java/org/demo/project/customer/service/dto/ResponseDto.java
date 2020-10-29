package org.demo.project.customer.service.dto;

import lombok.*;
import org.demo.project.customer.service.entity.Result;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResponseDto {
    private Result result;
    private Object additionalInfo;
}
