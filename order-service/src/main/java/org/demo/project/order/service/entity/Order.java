package org.demo.project.order.service.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "ORDER_TABLE")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long customerId;
    private BigDecimal amount;
    private State state;
    private RejectionReason rejectionReason;

    public enum State {
        PENDING,
        APPROVED,
        REJECTED;
    }

    public enum RejectionReason {
        INSUFFICIENT_CREDIT,
        UNKNOWN_CUSTOMER;
    }
}
