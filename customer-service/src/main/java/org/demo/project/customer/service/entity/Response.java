package org.demo.project.customer.service.entity;

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
