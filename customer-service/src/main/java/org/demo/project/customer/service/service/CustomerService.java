package org.demo.project.customer.service.service;

import org.demo.project.customer.service.entity.Customer;
import org.demo.project.customer.service.entity.Response;
import org.demo.project.customer.service.entity.Result;
import org.demo.project.customer.service.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    public Response createCustomer(String name, BigDecimal creditLimit) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setCreditLimit(creditLimit);
        customer.setCreditReservation(new BigDecimal(0));
        customerRepository.saveAndFlush(customer);
        return new Response(Result.SUCCESS, customer);
    }

    public Response getCustomerById(Long id) {
        return new Response(Result.SUCCESS, customerRepository.getCustomerById(id));
    }

    public Response getAllCustomers() {
        return new Response(Result.SUCCESS, customerRepository.findAll());
    }

    public Response reserveCredit(Long customerId, BigDecimal creditReservation) {
        Customer customer = customerRepository.getCustomerById(customerId);
        if (customer.getCreditLimit().compareTo(creditReservation) >= 0) {
            customer.setCreditLimit(customer.getCreditLimit().subtract(creditReservation));
            customer.setCreditReservation(customer.getCreditReservation().add(creditReservation));
            customerRepository.saveAndFlush(customer);
            return new Response(Result.SUCCESS, String.format("%s is successfully reserved for customer = %s", creditReservation, customerId));
        } else {
            return new Response(Result.FAILURE, String.format("Customer[id = %s, creditLimit = %s] doesn't have enough credit.", customerId, customer.getCreditLimit()));
        }
    }

    public Response releaseCredit(Long customerId, BigDecimal creditRelease) {
        Customer customer = customerRepository.getCustomerById(customerId);
        if (customer.getCreditReservation().compareTo(creditRelease) >= 0) {
            customer.setCreditReservation(customer.getCreditReservation().subtract(creditRelease));
            customer.setCreditLimit(customer.getCreditLimit().add(creditRelease));
            customerRepository.saveAndFlush(customer);
            return new Response(Result.SUCCESS, String.format("%s is successfully released for customer = %s", creditRelease, customerId));
        } else {
            return new Response(Result.FAILURE, String.format("Customer[id = %s, creditReservation = %s] doesn't have enough credit to release.", customerId, customer.getCreditReservation()));
        }
    }
}
