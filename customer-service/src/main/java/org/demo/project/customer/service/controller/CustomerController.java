package org.demo.project.customer.service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.project.customer.service.dto.ReleaseCreditDto;
import org.demo.project.customer.service.dto.ReserveCreditDto;
import org.demo.project.customer.service.entity.Response;
import org.demo.project.customer.service.entity.Result;
import org.demo.project.customer.service.dto.CreateCustomerDto;
import org.demo.project.customer.service.dto.ResponseDto;
import org.demo.project.customer.service.entity.Customer;
import org.demo.project.customer.service.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @GetMapping(value = "/getCustomerById/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto getCustomerById(@PathVariable Long id) {
        Response response = customerService.getCustomerById(id);
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @GetMapping(value = "/getAllCustomers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto getAllCustomers() {
        Response response = customerService.getAllCustomers();
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @PostMapping(value = "/createCustomer", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto createCustomer(@RequestBody CreateCustomerDto createCustomerDto) {
        Response response = customerService.createCustomer(createCustomerDto.getName(), new BigDecimal(createCustomerDto.getCreditLimit()));
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @PostMapping(value = "/reserveCredit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto reserveCredit(@RequestBody ReserveCreditDto reserveCreditDto) {
        Response response = customerService.reserveCredit(reserveCreditDto.getCustomerId(), new BigDecimal(reserveCreditDto.getCreditReservation()));
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }

    @PostMapping(value = "/releaseCredit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto releaseCredit(@RequestBody ReleaseCreditDto releaseCreditDto) {
        Response response = customerService.releaseCredit(releaseCreditDto.getCustomerId(), new BigDecimal(releaseCreditDto.getCreditRelease()));
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }
}
