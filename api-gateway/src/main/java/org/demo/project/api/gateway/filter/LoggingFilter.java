package org.demo.project.api.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class LoggingFilter extends ZuulFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        logger.info("Services ----> " + discoveryClient.getServices().toString());
        logger.info("Customer Service ----> " + discoveryClient.getInstances("customer-service").get(0).getUri().toString());
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        logger.info(String.format("%s is sent to %s", request.getMethod(), request.getRequestURI()));
        return null;
    }
}
