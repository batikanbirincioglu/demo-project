package org.demo.project.customer.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerController {
    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping("/services/{applicationName}")
    public List<ServiceInstance> getServices(@PathVariable String applicationName) {
        return discoveryClient.getInstances(applicationName);
    }
}
