package org.demo.project.order.service.repository;

import org.demo.project.order.service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> getOrdersByCustomerId(Long customerId);

    Order getOrderById(Long id);
}
