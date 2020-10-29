package org.demo.project.order.service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.demo.project.order.service.dto.*;
import org.demo.project.order.service.entity.Order;
import org.demo.project.order.service.entity.Response;
import org.demo.project.order.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/getAllOrders")
    public ResponseDto getAllOrders() throws JsonProcessingException {
        Response response = orderService.getAllOrders();
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @GetMapping("/getOrderById/{id}")
    public ResponseDto getOrderById(@PathVariable Long id) throws JsonProcessingException {
        Response response = orderService.getOrderById(id);
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @GetMapping("/getOrdersByCustomerId/{customerId}")
    public ResponseDto getOrdersByCustomerId(@PathVariable Long customerId) throws JsonProcessingException {
        Response response = orderService.getOrdersByCustomerId(customerId);
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @PostMapping(value = "/createOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto createOrder(@RequestBody CreateOrderDto createOrderDto) {
        Response response = orderService.createOrder(createOrderDto.getCustomerId(), new BigDecimal(createOrderDto.getAmount()));
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @PutMapping(value = "/approveOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto approveOrder(@RequestBody ApproveOrderDto approveOrderDto) {
        Response response = orderService.approveOrder(approveOrderDto.getOrderId());
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @PutMapping(value = "/rejectOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto rejectOrder(@RequestBody RejectOrderDto rejectOrderDto) {
        Response response = orderService.rejectOrder(rejectOrderDto.getOrderId(), Order.RejectionReason.valueOf(rejectOrderDto.getRejectionReason()));
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @DeleteMapping(value = "/deleteOrder/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto deleteOrder(@PathVariable Long id) {
        Response response = orderService.deleteOrder(id);
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }
}
