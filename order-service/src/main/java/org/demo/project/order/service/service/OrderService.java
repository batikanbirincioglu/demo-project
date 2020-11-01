package org.demo.project.order.service.service;

import org.demo.project.order.service.entity.Order;
import org.demo.project.order.service.entity.Response;
import org.demo.project.order.service.entity.Result;
import org.demo.project.order.service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private int numberOfApprovals = 0;

    @Autowired
    private OrderRepository orderRepository;

    public Response createOrder(Long customerId, BigDecimal amount) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setAmount(amount);
        order.setState(Order.State.PENDING);
        orderRepository.saveAndFlush(order);
        return new Response(Result.SUCCESS, order);
    }

    public Response approveOrder(Long id) {
        // Simulate database exception
        numberOfApprovals++;
        if (numberOfApprovals % 3 == 0) {
            return new Response(Result.FAILURE, "Database exception occurred.");
        }
        Order order = orderRepository.getOrderById(id);
        order.setState(Order.State.APPROVED);
        orderRepository.saveAndFlush(order);
        return new Response(Result.SUCCESS, String.format("Order = %s is approved.", id));
    }

    public Response rejectOrder(Long id, Order.RejectionReason rejectionReason) {
        Order order = orderRepository.getOrderById(id);
        order.setState(Order.State.REJECTED);
        order.setRejectionReason(rejectionReason);
        orderRepository.saveAndFlush(order);
        return new Response(Result.SUCCESS, String.format("Order = %s is rejected.", id));
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
        orderRepository.flush();
    }

    public Response getOrdersByCustomerId(Long customerId) {
        return new Response(Result.SUCCESS, orderRepository.getOrdersByCustomerId(customerId));
    }

    public Response getOrderById(Long id) {
        return new Response(Result.SUCCESS, orderRepository.getOrderById(id));
    }

    public Response getAllOrders() {
        return new Response(Result.SUCCESS, orderRepository.findAll());
    }
}
