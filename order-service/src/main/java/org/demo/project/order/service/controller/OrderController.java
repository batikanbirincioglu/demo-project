package org.demo.project.order.service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.demo.project.order.service.dto.*;
import org.demo.project.order.service.entity.Order;
import org.demo.project.order.service.entity.Response;
import org.demo.project.order.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.Media;
import java.math.BigDecimal;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping(value = "/getAllOrders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto getAllOrders() {
        Response response = orderService.getAllOrders();
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @GetMapping(value = "/getOrderById/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto getOrderById(@PathVariable Long id) {
        Response response = orderService.getOrderById(id);
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @GetMapping(value = "/getOrdersByCustomerId/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto getOrdersByCustomerId(@PathVariable Long customerId) {
        Response response = orderService.getOrdersByCustomerId(customerId);
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @PostMapping(value = "/createOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto createOrder(@RequestBody CreateOrderDto createOrderDto) {
        Response response = orderService.createOrder(createOrderDto.getCustomerId(), new BigDecimal(createOrderDto.getAmount()));
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @PostMapping(value = "/approveOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto approveOrder(@RequestBody ApproveOrderDto approveOrderDto) {
        Response response = orderService.approveOrder(approveOrderDto.getOrderId());
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @PostMapping(value = "/rejectOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto rejectOrder(@RequestBody RejectOrderDto rejectOrderDto) {
        Response response = orderService.rejectOrder(rejectOrderDto.getOrderId(), Order.RejectionReason.valueOf(rejectOrderDto.getRejectionReason()));
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @DeleteMapping(value = "/deleteOrder/{id}")
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}
