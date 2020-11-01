package org.demo.project.order.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.core.impl.provider.entity.XMLRootObjectProvider;
import org.demo.project.order.service.dto.ApproveOrderDto;
import org.demo.project.order.service.dto.CreateOrderDto;
import org.demo.project.order.service.dto.RejectOrderDto;
import org.demo.project.order.service.dto.ResponseDto;
import org.demo.project.order.service.entity.Order;
import org.demo.project.order.service.entity.Response;
import org.demo.project.order.service.entity.Result;
import org.demo.project.order.service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = {"/bootstrap.yml", "/application.yml"})
public class OrderControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    private OrderRepository orderRepository;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port;
        orderRepository.deleteAll();
        orderRepository.flush();
        restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Arrays.asList(new MappingJackson2HttpMessageConverter()));
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreateOrder() {
        CreateOrderDto createOrderDto = createCreateOrderDto(1L, "10");
        ResponseDto responseDto = createOrder(createOrderDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        Order order = objectMapper.convertValue(responseDto.getAdditionalInfo(), Order.class);
        assertEquals(createOrderDto.getCustomerId(), order.getCustomerId());
        assertEquals(createOrderDto.getAmount(), order.getAmount().toBigInteger().toString());
    }

    @Test
    public void testGetOrderById() {
        CreateOrderDto createOrderDto = createCreateOrderDto(1L, "10");
        ResponseDto responseDto = createOrder(createOrderDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        Order orderCreated = objectMapper.convertValue(responseDto.getAdditionalInfo(), Order.class);

        createCreateOrderDto(2L, "20");
        responseDto = createOrder(createOrderDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());

        createCreateOrderDto(3L, "30");
        responseDto = createOrder(createOrderDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());

        responseDto = getOrderById(orderCreated.getId());
        assertEquals(Result.SUCCESS, responseDto.getResult());
        Order orderFetched = objectMapper.convertValue(responseDto.getAdditionalInfo(), Order.class);
        assertEquals(orderCreated.getId(), orderFetched.getId());
        assertEquals(orderCreated.getCustomerId(), orderFetched.getCustomerId());
        assertEquals(orderCreated.getAmount().toBigInteger().toString(), orderFetched.getAmount().toBigInteger().toString());
        assertEquals(orderCreated.getState(), orderFetched.getState());
        assertEquals(orderCreated.getRejectionReason(), orderFetched.getRejectionReason());
    }

    @Test
    public void testGetAllOrders() {
        CreateOrderDto createOrderDto1 = createCreateOrderDto(1L, "10");
        CreateOrderDto createOrderDto2 = createCreateOrderDto(2L, "20");
        CreateOrderDto createOrderDto3 = createCreateOrderDto(3L, "30");

        createOrder(createOrderDto1);
        createOrder(createOrderDto2);
        createOrder(createOrderDto3);

        String url = baseUrl + "/getAllOrders";
        ResponseDto responseDto = restTemplate.getForEntity(url, ResponseDto.class).getBody();
        assertEquals(Result.SUCCESS, responseDto.getResult());
        List<Object> list = objectMapper.convertValue(responseDto.getAdditionalInfo(), List.class);
        List<Order> orders = list.stream().map(el -> objectMapper.convertValue(el, Order.class)).collect(Collectors.toList());
        assertEquals(3, orders.size());
        assertEquals(createOrderDto1.getCustomerId(), orders.get(0).getCustomerId());
        assertEquals(createOrderDto1.getAmount(), orders.get(0).getAmount().toBigInteger().toString());
        assertEquals(createOrderDto2.getCustomerId(), orders.get(1).getCustomerId());
        assertEquals(createOrderDto2.getAmount(), orders.get(1).getAmount().toBigInteger().toString());
        assertEquals(createOrderDto3.getCustomerId(), orders.get(2).getCustomerId());
        assertEquals(createOrderDto3.getAmount(), orders.get(2).getAmount().toBigInteger().toString());
    }

    @Test
    public void testGetOrdersByCustomerId() {
        CreateOrderDto createOrderDto1 = createCreateOrderDto(1L, "10");
        CreateOrderDto createOrderDto2 = createCreateOrderDto(2L, "20");
        CreateOrderDto createOrderDto3 = createCreateOrderDto(1L, "30");

        createOrder(createOrderDto1);
        createOrder(createOrderDto2);
        createOrder(createOrderDto3);

        String url = baseUrl + "/getOrdersByCustomerId/{customerId}";
        ResponseDto responseDto = restTemplate.getForEntity(url, ResponseDto.class, 1L).getBody();
        assertEquals(Result.SUCCESS, responseDto.getResult());
        List<Object> list = objectMapper.convertValue(responseDto.getAdditionalInfo(), List.class);
        List<Order> orders = list.stream().map(el -> objectMapper.convertValue(el, Order.class)).collect(Collectors.toList());
        assertEquals(2, orders.size());
        assertEquals(createOrderDto1.getCustomerId(), orders.get(0).getCustomerId());
        assertEquals(createOrderDto1.getAmount(), orders.get(0).getAmount().toBigInteger().toString());
        assertEquals(createOrderDto3.getCustomerId(), orders.get(1).getCustomerId());
        assertEquals(createOrderDto3.getAmount(), orders.get(1).getAmount().toBigInteger().toString());
    }

    @Test
    public void testApproveOrder() {
        CreateOrderDto createOrderDto1 = createCreateOrderDto(1L, "10");
        ResponseDto responseDto = createOrder(createOrderDto1);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        Order order = objectMapper.convertValue(responseDto.getAdditionalInfo(), Order.class);
        assertNotNull(order.getId());
        assertEquals(1L, order.getCustomerId());
        assertEquals("10", order.getAmount().toBigInteger().toString());
        assertEquals(Order.State.PENDING, order.getState());
        assertNull(order.getRejectionReason());

        ApproveOrderDto approveOrderDto = createApproveOrderDto(order.getId());
        responseDto = approveOrder(approveOrderDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertEquals(String.format("Order = %s is approved.", order.getId()), responseDto.getAdditionalInfo());

        responseDto = getOrderById(order.getId());
        assertEquals(Result.SUCCESS, responseDto.getResult());
        order = objectMapper.convertValue(responseDto.getAdditionalInfo(), Order.class);
        assertEquals(Order.State.APPROVED, order.getState());
    }

    @Test
    public void testRejectOrder() {
        CreateOrderDto createOrderDto1 = createCreateOrderDto(1L, "10");
        ResponseDto responseDto = createOrder(createOrderDto1);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        Order order = objectMapper.convertValue(responseDto.getAdditionalInfo(), Order.class);
        assertNotNull(order.getId());
        assertEquals(1L, order.getCustomerId());
        assertEquals("10", order.getAmount().toBigInteger().toString());
        assertEquals(Order.State.PENDING, order.getState());
        assertNull(order.getRejectionReason());

        RejectOrderDto rejectOrderDto = createRejectOrderDto(order.getId(), "INSUFFICIENT_CREDIT");
        responseDto = rejectOrder(rejectOrderDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertEquals(String.format("Order = %s is rejected.", order.getId()), responseDto.getAdditionalInfo());

        responseDto = getOrderById(order.getId());
        assertEquals(Result.SUCCESS, responseDto.getResult());
        order = objectMapper.convertValue(responseDto.getAdditionalInfo(), Order.class);
        assertEquals(Order.State.REJECTED, order.getState());
        assertEquals(Order.RejectionReason.INSUFFICIENT_CREDIT, order.getRejectionReason());
    }

    @Test
    public void testDeleteOrder() {
        CreateOrderDto createOrderDto1 = createCreateOrderDto(1L, "10");
        ResponseDto responseDto = createOrder(createOrderDto1);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        Order order = objectMapper.convertValue(responseDto.getAdditionalInfo(), Order.class);
        assertNotNull(order.getId());
        assertEquals(1L, order.getCustomerId());
        assertEquals("10", order.getAmount().toBigInteger().toString());

        String url = baseUrl + "/deleteOrder/{id}";
        restTemplate.delete(url, order.getId());

        responseDto = getOrderById(order.getId());
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertNull(responseDto.getAdditionalInfo());
    }

    private CreateOrderDto createCreateOrderDto(Long customerId, String amount) {
        return new CreateOrderDto(customerId, amount);
    }

    private ApproveOrderDto createApproveOrderDto(Long orderId) {
        return new ApproveOrderDto(orderId);
    }

    private RejectOrderDto createRejectOrderDto(Long orderId, String rejectionReason) {
        return new RejectOrderDto(orderId, rejectionReason);
    }

    private ResponseDto createOrder(CreateOrderDto createOrderDto) {
        String url = baseUrl + "/createOrder";
        return restTemplate.postForEntity(url, createOrderDto, ResponseDto.class).getBody();
    }

    private ResponseDto approveOrder(ApproveOrderDto approveOrderDto) {
        String url = baseUrl + "/approveOrder";
        return restTemplate.postForEntity(url, approveOrderDto, ResponseDto.class).getBody();
    }

    private ResponseDto rejectOrder(RejectOrderDto rejectOrderDto) {
        String url = baseUrl + "/rejectOrder";
        return restTemplate.postForEntity(url, rejectOrderDto, ResponseDto.class).getBody();
    }

    private ResponseDto getOrderById(Long id) {
        String url = baseUrl + "/getOrderById/{id}";
        return restTemplate.getForEntity(url, ResponseDto.class, id).getBody();
    }
}
