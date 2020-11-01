package org.demo.project.orchestrator.client;

import org.demo.project.orchestrator.dto.ApproveOrderDto;
import org.demo.project.orchestrator.dto.CreateOrderDto;
import org.demo.project.orchestrator.dto.RejectOrderDto;
import org.demo.project.orchestrator.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Path;

@FeignClient("order-service")
public interface OrderServiceClient {
    @PostMapping(value = "/createOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseDto createOrder(@RequestBody CreateOrderDto createOrderDto);

    @PostMapping(value = "/approveOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseDto approveOrder(@RequestBody ApproveOrderDto approveOrderDto);

    @PostMapping(value = "/rejectOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseDto rejectOrder(@RequestBody RejectOrderDto rejectOrderDto);

    @DeleteMapping(value = "/deleteOrder/{id}")
    void deleteOrder(@PathVariable Long id);
}
