package org.demo.project.orchestrator.dto;

import lombok.*;
import org.demo.project.orchestrator.entity.Result;

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
