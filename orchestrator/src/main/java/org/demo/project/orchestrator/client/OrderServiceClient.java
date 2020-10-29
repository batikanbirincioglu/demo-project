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
    public ResponseDto createOrder(@RequestBody CreateOrderDto createOrderDto);

    @PutMapping(value = "/approveOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto approveOrder(@RequestBody ApproveOrderDto approveOrderDto);

    @PutMapping(value = "/rejectOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto rejectOrder(@RequestBody RejectOrderDto rejectOrderDto);

    @DeleteMapping(value = "/deleteOrder/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto deleteOrder(@PathVariable Long id);
}
