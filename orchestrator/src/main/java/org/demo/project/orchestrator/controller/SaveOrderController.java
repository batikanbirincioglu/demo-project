package org.demo.project.orchestrator.controller;

import org.demo.project.orchestrator.dto.ResponseDto;
import org.demo.project.orchestrator.dto.SaveOrderDto;
import org.demo.project.orchestrator.entity.Response;
import org.demo.project.orchestrator.entity.Result;
import org.demo.project.orchestrator.service.SaveOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class SaveOrderController {
    private static final Logger logger = LoggerFactory.getLogger(SaveOrderController.class);

    @Autowired
    private SaveOrderService saveOrderService;

    @PostMapping(value = "/saveOrder", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto saveOrder(@RequestBody SaveOrderDto saveOrderDto) {
        Response response = saveOrderService.saveOrder(saveOrderDto.getCustomerId(), new BigDecimal(saveOrderDto.getAmount()));
        return new ResponseDto(response.getResult(), response.getAdditionalInfo());
    }
}
