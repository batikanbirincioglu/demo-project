package org.demo.project.orchestrator.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Response {
    private Result result;
    private Object additionalInfo;
}
