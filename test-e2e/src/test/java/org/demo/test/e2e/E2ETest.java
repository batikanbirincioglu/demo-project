package org.demo.test.e2e;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.support.hierarchical.OpenTest4JAwareThrowableCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class E2ETest {
    private static final Logger logger = LoggerFactory.getLogger(E2ETest.class);

    private static String configServerName = "config-server_1";
    private static int configServerPort = 8080;
    private static String configServerBaseUrl;

    private static String serviceRegistryName = "service-registry_1";
    private static int serviceRegistryPort = 8081;
    private static String serviceRegistryBaseUrl;

    private static String apiGatewayName = "api-gateway_1";
    private static int apiGatewayPort = 8082;
    private static String apiGatewayBaseUrl;

    private static String orchestratorName = "orchestrator_1";
    private static int orchestratorPort = 8085;
    private static String orchestratorBaseUrl;

    private static String customerServiceName = "customer-service_1";
    private static int customerServicePort = 8084;
    private static String customerServiceBaseUrl;

    private static String orderServiceName = "order-service_1";
    private static int orderServicePort = 8083;
    private static String orderServiceBaseUrl;

    private static DockerComposeContainer dockerComposeContainer;
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void beforeAll() throws Exception {
        URL url = E2ETest.class.getClassLoader().getResource("docker-compose.yml");
        dockerComposeContainer = new DockerComposeContainer(new File(url.toURI()))
                .withExposedService(configServerName, configServerPort, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(200)))
                .withExposedService(serviceRegistryName, serviceRegistryPort, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(200)))
                .withExposedService(apiGatewayName, apiGatewayPort, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(200)))
                .withExposedService(orderServiceName, orderServicePort, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(200)))
                .withExposedService(customerServiceName, customerServicePort, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(200)))
                .withExposedService(orchestratorName, orchestratorPort, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(200)));

        dockerComposeContainer.start();
        configServerBaseUrl = getBaseUrl(configServerName, configServerPort);
        serviceRegistryBaseUrl = getBaseUrl(serviceRegistryName, serviceRegistryPort);
        apiGatewayBaseUrl = getBaseUrl(apiGatewayName, apiGatewayPort);
        orderServiceBaseUrl = getBaseUrl(orderServiceName, orderServicePort);
        customerServiceBaseUrl = getBaseUrl(customerServiceName, customerServicePort);
        orchestratorBaseUrl = getBaseUrl(orchestratorName, orchestratorPort);

        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
    }

    private static String getBaseUrl(String serviceName, int servicePort) {
        String baseUrl = String.format("http://%s:%s", dockerComposeContainer.getServiceHost(serviceName, servicePort), dockerComposeContainer.getServicePort(serviceName, servicePort));
        logger.info("Base url for {} is {}", serviceName, baseUrl);
        return baseUrl;
    }

    /**
     * Retries occur because during startup, api-gateway might not be able to fetch correct ip:port pairs for related services.
     * Thus for the first requests, we are retrying for couple of seconds...
     *
     * 1-) Create a customer
     * 2-) Save first order (approved)
     * 3-) Save second order (approved)
     * 4-) While saving third order, mimic database exception so that everything rollbacks from all services (rollback)
     * 5-) Save fourth order (approved)
     * 6-) Save fifth order (approved)
     * 7-) Couldn't save sixth order because of INSUFFICIENT_CREDIT (rejected)
     * 8-) Couldn't save seventh order because of UNKNOWN_CUSTOMER (rejected)
     */
    @Test
    public void testOrchestrator_UsingApiGateway() throws Exception {
        // Create Customer Using Api Gateway (Redirecting Request To Customer-Service)
        String result = retry(this::createCustomer, "batikan", "100", 30, 2000L);
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":{\"id\":1,\"name\":\"batikan\",\"creditLimit\":100,\"creditReservation\":0}}", result);
        Long customerId = objectMapper.readTree(result).get("additionalInfo").get("id").longValue();

        // Get All Customers Using Api Gateway (Redirecting Request To Customer-Service)
        result = getAllCustomers();
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":[{\"id\":1,\"name\":\"batikan\",\"creditLimit\":100.00,\"creditReservation\":0.00}]}", result);

        // Get Customer By Id Using Api Gateway (Redirecting Request To Customer-Service)
        result = getCustomerById(customerId);
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":{\"id\":1,\"name\":\"batikan\",\"creditLimit\":100.00,\"creditReservation\":0.00}}", result);

        // Save Order Using Api Gateway (Redirecting Request To Orchestrator)
        result = retry(this::saveOrder, customerId, "25", 30, 2000L);
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":\"Order = 1 is approved.\"}", result);

        // Get Customer By Id Using Api Gateway (Redirecting Request To Customer-Service)
        result = getCustomerById(customerId);
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":{\"id\":1,\"name\":\"batikan\",\"creditLimit\":75.00,\"creditReservation\":25.00}}", result);

        // Get All Orders Using Api Gateway (Redirecting Request To Order-Service)
        result = getAllOrders();
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":[{\"id\":1,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}]}", result);

        // Save Order Using Api Gateway (Redirecting Request To Orchestrator)
        result = saveOrder(customerId, "25");
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":\"Order = 2 is approved.\"}", result);

        // Get Customer By Id Using Api Gateway (Redirecting Request To Customer-Service)
        result = getCustomerById(customerId);
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":{\"id\":1,\"name\":\"batikan\",\"creditLimit\":50.00,\"creditReservation\":50.00}}", result);

        // Get All Orders Using Api Gateway (Redirecting Request To Order-Service)
        result = getAllOrders();
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":[" +
                "{\"id\":1,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":2,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}" +
                "]}", result);

        // Save Order Using Api Gateway (Redirecting Request To Orchestrator)
        result = saveOrder(customerId, "25");
        assertEquals("{\"result\":\"FAILURE\",\"additionalInfo\":" +
                "\"Database exception occurred while approving order, thus order = 3 got deleted && reserved-credit = 25 is released.\"}", result);

        // Get All Orders Using Api Gateway (Redirecting Request To Order-Service)
        result = getAllOrders();
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":[" +
                "{\"id\":1,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":2,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}" +
                "]}", result);

        // Get Customer By Id Using Api Gateway (Redirecting Request To Customer-Service)
        result = getCustomerById(customerId);
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":{\"id\":1,\"name\":\"batikan\",\"creditLimit\":50.00,\"creditReservation\":50.00}}", result);

        // Save Order Using Api Gateway (Redirecting Request To Orchestrator)
        result = saveOrder(customerId, "25");
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":\"Order = 4 is approved.\"}", result);

        // Get Customer By Id Using Api Gateway (Redirecting Request To Customer-Service)
        result = getCustomerById(customerId);
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":{\"id\":1,\"name\":\"batikan\",\"creditLimit\":25.00,\"creditReservation\":75.00}}", result);

        // Get All Orders Using Api Gateway (Redirecting Request To Order-Service)
        result = getAllOrders();
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":[" +
                "{\"id\":1,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":2,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":4,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}" +
                "]}", result);

        // Save Order Using Api Gateway (Redirecting Request To Orchestrator)
        result = saveOrder(customerId, "25");
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":\"Order = 5 is approved.\"}", result);

        // Get Customer By Id Using Api Gateway (Redirecting Request To Customer-Service)
        result = getCustomerById(customerId);
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":{\"id\":1,\"name\":\"batikan\",\"creditLimit\":0.00,\"creditReservation\":100.00}}", result);

        // Get All Orders Using Api Gateway (Redirecting Request To Order-Service)
        result = getAllOrders();
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":[" +
                "{\"id\":1,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":2,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":4,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":5,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}" +
                "]}", result);

        // Save Order Using Api Gateway (Redirecting Request To Orchestrator)
        result = saveOrder(customerId, "25");
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":\"Order = 6 is rejected.\"}", result);

        // Get Customer By Id Using Api Gateway (Redirecting Request To Customer-Service)
        result = getCustomerById(customerId);
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":{\"id\":1,\"name\":\"batikan\",\"creditLimit\":0.00,\"creditReservation\":100.00}}", result);

        // Get All Orders Using Api Gateway (Redirecting Request To Order-Service)
        result = getAllOrders();
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":[" +
                "{\"id\":1,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":2,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":4,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":5,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":6,\"customerId\":1,\"amount\":25.00,\"state\":\"REJECTED\",\"rejectionReason\":\"INSUFFICIENT_CREDIT\"}" +
                "]}", result);

        // Save Order Using Api Gateway (Redirecting Request To Orchestrator)
        result = saveOrder(981273498732L, "25");
        assertEquals("{\"result\":\"FAILURE\",\"additionalInfo\":\"No such customer with id = 981273498732, thus order = 7 got rejected.\"}", result);

        // Get Customer By Id Using Api Gateway (Redirecting Request To Customer-Service)
        result = getCustomerById(customerId);
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":{\"id\":1,\"name\":\"batikan\",\"creditLimit\":0.00,\"creditReservation\":100.00}}", result);

        // Get All Orders Using Api Gateway (Redirecting Request To Order-Service)
        result = getAllOrders();
        assertEquals("{\"result\":\"SUCCESS\",\"additionalInfo\":[" +
                "{\"id\":1,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":2,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":4,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":5,\"customerId\":1,\"amount\":25.00,\"state\":\"APPROVED\",\"rejectionReason\":null}," +
                "{\"id\":6,\"customerId\":1,\"amount\":25.00,\"state\":\"REJECTED\",\"rejectionReason\":\"INSUFFICIENT_CREDIT\"}," +
                "{\"id\":7,\"customerId\":981273498732,\"amount\":25.00,\"state\":\"REJECTED\",\"rejectionReason\":\"UNKNOWN_CUSTOMER\"}" +
                "]}", result);
    }

    private String retry(Function<Object, String> function, Object argument, int retryCount, long retryInterval) {
        for (int i = 0; i < retryCount; i++) {
            try {
                return function.apply(argument);
            } catch (Exception e) {
                try {
                    logger.error("{} occurred, sleeping {} for retry...", e.getClass(), retryInterval);
                    e.printStackTrace();
                    Thread.sleep(retryInterval);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    private String retry(BiFunction<String, String, String> biFunction, String argument1, String argument2, int retryCount, long retryInterval) {
        for (int i = 0; i < retryCount; i++) {
            try {
                return biFunction.apply(argument1, argument2);
            } catch (Exception e) {
                try {
                    logger.error("{} occurred, sleeping {} for retry...", e.getClass(), retryInterval);
                    e.printStackTrace();
                    Thread.sleep(retryInterval);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    private String retry(BiFunction<Long, String, String> biFunction, Long argument1, String argument2, int retryCount, long retryInterval) {
        for (int i = 0; i < retryCount; i++) {
            try {
                return biFunction.apply(argument1, argument2);
            } catch (Exception e) {
                try {
                    logger.error("{} occurred, sleeping {} for retry...", e.getClass(), retryInterval);
                    e.printStackTrace();
                    Thread.sleep(retryInterval);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    private String createCustomer(String name, String creditLimit) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(String.format("{\"name\": \"%s\", \"creditLimit\": \"%s\"}", name, creditLimit), httpHeaders);
        return restTemplate.postForObject(apiGatewayBaseUrl + "/customer-service/createCustomer", httpEntity, String.class);
    }

    private String getAllCustomers() {
        return restTemplate.getForObject(apiGatewayBaseUrl + "/customer-service/getAllCustomers", String.class);
    }

    private String getCustomerById(Long id) {
        return restTemplate.getForObject(apiGatewayBaseUrl + "/customer-service/getCustomerById/{id}", String.class, id);
    }

    private String getOrdersByCustomerId(Long customerId) {
        return restTemplate.getForObject(apiGatewayBaseUrl + "/order-service/getOrdersByCustomerId/{customerId}", String.class, customerId);
    }

    private String getOrderById(Long id) {
        return restTemplate.getForObject(apiGatewayBaseUrl + "/order-service/getOrderById/{id}", String.class, id);
    }

    private String getAllOrders() {
        return restTemplate.getForObject(apiGatewayBaseUrl + "/order-service/getAllOrders", String.class);
    }

    private String saveOrder(Long customerId, String amount) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(String.format("{\"customerId\": %s, \"amount\": \"%s\"}", customerId, amount), httpHeaders);
        return restTemplate.postForObject(apiGatewayBaseUrl + "/orchestrator/saveOrder", httpEntity, String.class);
    }
}
