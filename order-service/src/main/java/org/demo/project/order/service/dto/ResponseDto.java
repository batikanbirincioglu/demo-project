package org.demo.project.order.service.dto;

import lombok.*;
import org.demo.project.order.service.entity.Result;

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
