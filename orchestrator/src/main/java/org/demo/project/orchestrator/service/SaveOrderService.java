package org.demo.project.orchestrator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.project.orchestrator.client.CustomerServiceClient;
import org.demo.project.orchestrator.client.OrderServiceClient;
import org.demo.project.orchestrator.dto.*;
import org.demo.project.orchestrator.entity.Response;
import org.demo.project.orchestrator.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class SaveOrderService {
    @Autowired
    private CustomerServiceClient customerServiceClient;
    @Autowired
    private OrderServiceClient orderServiceClient;

    public Response saveOrder(Long customerId, BigDecimal amount) {
        Response response = new Response();
        ResponseDto responseDto = orderServiceClient.createOrder(new CreateOrderDto(customerId, amount.toString()));
        if (responseDto.getResult().equals(Result.SUCCESS)) {
            Long orderId = getOrderId(responseDto);
            if (customerExists(customerId)) {
                responseDto = customerServiceClient.reserveCredit(new ReserveCreditDto(customerId, amount.toString()));
                if (responseDto.getResult().equals(Result.SUCCESS)) {
                    responseDto = orderServiceClient.approveOrder(new ApproveOrderDto(orderId));
                } else {
                    responseDto = orderServiceClient.rejectOrder(new RejectOrderDto(orderId, "INSUFFICIENT_CREDIT"));
                }
                // SAGA PATTERN (COMPENSATION MECHANISM EXAMPLE IN CASE OF DATABASE EXCEPTION)
                if (responseDto.getResult().equals(Result.FAILURE) && responseDto.getAdditionalInfo().toString().startsWith("Database exception")) {
                    response.setResult(responseDto.getResult());
                    orderServiceClient.deleteOrder(orderId);
                    customerServiceClient.releaseCredit(new ReleaseCreditDto(customerId, amount.toString()));
                    response.setAdditionalInfo(String.format("Database exception occurred while approving order, thus order = %s got deleted && reserved-credit = %s is released.", orderId, amount));
                } else {
                    response.setResult(responseDto.getResult());
                    response.setAdditionalInfo(responseDto.getAdditionalInfo());
                }
            } else {
                orderServiceClient.rejectOrder(new RejectOrderDto(orderId, "UNKNOWN_CUSTOMER"));
                response.setResult(Result.FAILURE);
                response.setAdditionalInfo(String.format("No such customer with id = %s, thus order = %s got rejected.", customerId, orderId));
            }
        } else {
            response.setResult(responseDto.getResult());
            response.setAdditionalInfo(responseDto.getAdditionalInfo());
        }
        return response;
    }

    private boolean customerExists(Long customerId) {
        return customerServiceClient.getCustomerById(customerId).getAdditionalInfo() != null;
    }

    private Long getOrderId(ResponseDto responseDto) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(responseDto.getAdditionalInfo());
            return Long.valueOf(new ObjectMapper().readValue(json, Map.class).get("id").toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
