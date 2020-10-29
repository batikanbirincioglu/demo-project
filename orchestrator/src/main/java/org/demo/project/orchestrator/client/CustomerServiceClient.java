package org.demo.project.orchestrator.client;

import org.demo.project.orchestrator.dto.ReleaseCreditDto;
import org.demo.project.orchestrator.dto.ReserveCreditDto;
import org.demo.project.orchestrator.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient("customer-service")
public interface CustomerServiceClient {
    @PutMapping(value = "/reserveCredit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto reserveCredit(@RequestBody ReserveCreditDto reserveCreditDto);

    @PutMapping(value = "/releaseCredit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto releaseCredit(@RequestBody ReleaseCreditDto releaseCreditDto);

    @GetMapping(value = "/getCustomerById/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto getCustomerById(@PathVariable Long id);
}
