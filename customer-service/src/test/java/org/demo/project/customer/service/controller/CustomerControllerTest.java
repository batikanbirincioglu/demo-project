package org.demo.project.customer.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.project.customer.service.dto.CreateCustomerDto;
import org.demo.project.customer.service.dto.ReleaseCreditDto;
import org.demo.project.customer.service.dto.ReserveCreditDto;
import org.demo.project.customer.service.dto.ResponseDto;
import org.demo.project.customer.service.entity.Customer;
import org.demo.project.customer.service.entity.Result;
import org.demo.project.customer.service.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"/bootstrap.yml", "/application.yml"})
public class CustomerControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    @Autowired
    private CustomerRepository customerRepository;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port;
        customerRepository.deleteAll();
        customerRepository.flush();
        restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Arrays.asList(new MappingJackson2HttpMessageConverter()));
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreateCustomer() {
        CreateCustomerDto createCustomerDto = createCreateCustomerDto("batikan", "100");
        Customer customer = createCustomer(createCustomerDto);
        assertEquals(createCustomerDto.getName(), customer.getName());
        assertEquals(createCustomerDto.getCreditLimit(), customer.getCreditLimit().toString());
    }

    @Test
    public void testGetAllCustomers() {
        String url = baseUrl + "/getAllCustomers";

        ResponseEntity<ResponseDto> responseEntity = restTemplate.getForEntity(url, ResponseDto.class);
        ResponseDto responseDto = responseEntity.getBody();
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertEquals(new ArrayList(), responseDto.getAdditionalInfo());

        CreateCustomerDto createCustomerDto1 = createCreateCustomerDto("name1", "100");
        CreateCustomerDto createCustomerDto2 = createCreateCustomerDto("name2", "200");
        CreateCustomerDto createCustomerDto3 = createCreateCustomerDto("name3", "300");

        createCustomer(createCustomerDto1);
        createCustomer(createCustomerDto2);
        createCustomer(createCustomerDto3);

        responseDto = restTemplate.getForEntity(url, ResponseDto.class).getBody();
        assertEquals(Result.SUCCESS, responseDto.getResult());
        List<Object> list = objectMapper.convertValue(responseDto.getAdditionalInfo(), List.class);
        List<Customer> customers = list.stream().map(el -> objectMapper.convertValue(el, Customer.class)).collect(Collectors.toList());
        assertEquals(3, customers.size());
        assertEquals(createCustomerDto1.getName(), customers.get(0).getName());
        assertEquals(createCustomerDto1.getCreditLimit(), customers.get(0).getCreditLimit().toBigInteger().toString());
        assertEquals(createCustomerDto2.getName(), customers.get(1).getName());
        assertEquals(createCustomerDto2.getCreditLimit(), customers.get(1).getCreditLimit().toBigInteger().toString());
        assertEquals(createCustomerDto3.getName(), customers.get(2).getName());
        assertEquals(createCustomerDto3.getCreditLimit(), customers.get(2).getCreditLimit().toBigInteger().toString());
    }

    @Test
    public void testGetCustomerById() {
        CreateCustomerDto createCustomerDto1 = createCreateCustomerDto("name1", "100");
        CreateCustomerDto createCustomerDto2 = createCreateCustomerDto("name2", "200");
        CreateCustomerDto createCustomerDto3 = createCreateCustomerDto("name3", "300");

        createCustomer(createCustomerDto1);
        createCustomer(createCustomerDto2);
        createCustomer(createCustomerDto3);

        Customer customer = getCustomerById(2L);
        assertEquals(createCustomerDto2.getName(), customer.getName());
        assertEquals(createCustomerDto2.getCreditLimit(), customer.getCreditLimit().toBigInteger().toString());
    }

    @Test
    public void testReserveCredit() {
        CreateCustomerDto createCustomerDto = createCreateCustomerDto("name1", "100");
        Customer customer = createCustomer(createCustomerDto);

        ReserveCreditDto reserveCreditDto = createReserveCreditDto(customer.getId(), "25");
        ResponseDto responseDto = reserveCredit(reserveCreditDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertEquals("25 is successfully reserved for customer = " + customer.getId(), responseDto.getAdditionalInfo());
        customer = getCustomerById(customer.getId());
        assertEquals("75", customer.getCreditLimit().toBigInteger().toString());
        assertEquals("25", customer.getCreditReservation().toBigInteger().toString());

        reserveCreditDto = createReserveCreditDto(customer.getId(), "25");
        responseDto = reserveCredit(reserveCreditDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertEquals("25 is successfully reserved for customer = " + customer.getId(), responseDto.getAdditionalInfo());
        customer = getCustomerById(customer.getId());
        assertEquals("50", customer.getCreditLimit().toBigInteger().toString());
        assertEquals("50", customer.getCreditReservation().toBigInteger().toString());

        reserveCreditDto = createReserveCreditDto(customer.getId(), "25");
        responseDto = reserveCredit(reserveCreditDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertEquals("25 is successfully reserved for customer = " + customer.getId(), responseDto.getAdditionalInfo());
        customer = getCustomerById(customer.getId());
        assertEquals("25", customer.getCreditLimit().toBigInteger().toString());
        assertEquals("75", customer.getCreditReservation().toBigInteger().toString());

        reserveCreditDto = createReserveCreditDto(customer.getId(), "25");
        responseDto = reserveCredit(reserveCreditDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertEquals("25 is successfully reserved for customer = " + customer.getId(), responseDto.getAdditionalInfo());
        customer = getCustomerById(customer.getId());
        assertEquals("0", customer.getCreditLimit().toBigInteger().toString());
        assertEquals("100", customer.getCreditReservation().toBigInteger().toString());

        reserveCreditDto = createReserveCreditDto(customer.getId(), "25");
        responseDto = reserveCredit(reserveCreditDto);
        assertEquals(Result.FAILURE, responseDto.getResult());
        assertEquals(String.format("Customer[id = %s, creditLimit = 0.00] doesn't have enough credit.", customer.getId()), responseDto.getAdditionalInfo());
    }

    @Test
    public void testReleaseCredit() {
        CreateCustomerDto createCustomerDto = createCreateCustomerDto("name1", "100");
        Customer customer = createCustomer(createCustomerDto);

        ReserveCreditDto reserveCreditDto = createReserveCreditDto(customer.getId(), "25");
        ResponseDto responseDto = reserveCredit(reserveCreditDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertEquals("25 is successfully reserved for customer = " + customer.getId(), responseDto.getAdditionalInfo());
        customer = getCustomerById(customer.getId());
        assertEquals("75", customer.getCreditLimit().toBigInteger().toString());
        assertEquals("25", customer.getCreditReservation().toBigInteger().toString());

        ReleaseCreditDto releaseCreditDto = createReleaseCreditDto(customer.getId(), "10");
        responseDto = releaseCredit(releaseCreditDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertEquals("10 is successfully released for customer = " + customer.getId(), responseDto.getAdditionalInfo());

        releaseCreditDto = createReleaseCreditDto(customer.getId(), "10");
        responseDto = releaseCredit(releaseCreditDto);
        assertEquals(Result.SUCCESS, responseDto.getResult());
        assertEquals("10 is successfully released for customer = " + customer.getId(), responseDto.getAdditionalInfo());

        releaseCreditDto = createReleaseCreditDto(customer.getId(), "10");
        responseDto = releaseCredit(releaseCreditDto);
        assertEquals(Result.FAILURE, responseDto.getResult());
        customer = getCustomerById(customer.getId());
        assertEquals(String.format("Customer[id = %s, creditReservation = %s0] doesn't have enough credit to release.", customer.getId(), customer.getCreditReservation()), responseDto.getAdditionalInfo());
    }

    private ReserveCreditDto createReserveCreditDto(Long customerId, String creditReservation) {
        return new ReserveCreditDto(customerId, creditReservation);
    }

    private CreateCustomerDto createCreateCustomerDto(String name, String creditLimit) {
        return new CreateCustomerDto(name, creditLimit);
    }

    private ReleaseCreditDto createReleaseCreditDto(Long customerId, String creditRelease) {
        ReleaseCreditDto releaseCreditDto = new ReleaseCreditDto();
        releaseCreditDto.setCustomerId(customerId);
        releaseCreditDto.setCreditRelease(creditRelease);
        return releaseCreditDto;
    }

    private ResponseDto reserveCredit(ReserveCreditDto reserveCreditDto) {
        String url = baseUrl + "/reserveCredit";
        return restTemplate.postForEntity(url, reserveCreditDto, ResponseDto.class).getBody();
    }

    private Customer createCustomer(CreateCustomerDto createCustomerDto) {
        String url = baseUrl + "/createCustomer";

        ResponseEntity<ResponseDto> responseEntity = restTemplate.postForEntity(url, createCustomerDto, ResponseDto.class);
        ResponseDto responseDto = responseEntity.getBody();
        assertEquals(Result.SUCCESS, responseDto.getResult());
        return objectMapper.convertValue(responseDto.getAdditionalInfo(), Customer.class);
    }

    private ResponseDto releaseCredit(ReleaseCreditDto releaseCreditDto) {
        String url = baseUrl + "/releaseCredit";
        return restTemplate.postForEntity(url, releaseCreditDto, ResponseDto.class).getBody();
    }

    private Customer getCustomerById(Long id) {
        String url = baseUrl + "/getCustomerById/{id}";

        ResponseEntity<ResponseDto> responseEntity = restTemplate.getForEntity(url, ResponseDto.class, id);
        ResponseDto responseDto = responseEntity.getBody();
        assertEquals(Result.SUCCESS, responseDto.getResult());
        return objectMapper.convertValue(responseDto.getAdditionalInfo(), Customer.class);
    }
}
