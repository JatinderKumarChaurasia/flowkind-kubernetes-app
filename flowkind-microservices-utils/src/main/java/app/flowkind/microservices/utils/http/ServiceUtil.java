package app.flowkind.microservices.utils.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ServiceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtil.class);
    private final String port;
    private String serviceAddress = null;

    @Autowired
    public ServiceUtil(@Value("${server.port}") String port) {
        this.port = port;
    }

    public String getServiceAddress() {
        if (serviceAddress == null) {
            serviceAddress = findMyHostName() + "/"+findMyIPAddress()+":"+port;
            LOGGER.info("Service Address is: {}",serviceAddress);
        }
        LOGGER.info("Service Address is: {}",serviceAddress);
        return serviceAddress;
    }

    private String findMyHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOGGER.info("Unknown Host Name: {}",e.getMessage());
            return "Unknown Host Name: " + e.getMessage();
        }
    }

    private String findMyIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.info("Unknown IP Address: {}",e.getMessage());
            return "Unknown IP Address: " + e.getMessage();
        }
    }
}
