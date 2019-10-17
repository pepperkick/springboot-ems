package com.pepperkick.ems.server.config;

import com.pepperkick.ems.server.entity.Employee;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class IgniteCacheConfiguration {
    @Value("spring.profiles.active")
    private String profile;

    @Bean
    public Ignite igniteInstance() {
        Dotenv env = Dotenv.load();
        if (env.get("USE_IGNITE", "false").equalsIgnoreCase("true")) {
            boolean isClient = env.get("IGNITE_CLIENT", "false").equalsIgnoreCase("true");
            IgniteConfiguration config = new IgniteConfiguration();

            ArrayList<String> addresses = new ArrayList<>();
            TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();
            TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();

            if (isClient) {
                config.setClientMode(true);
                String address = env.get("IGNITE_HOST", "localhost") + ":" + env.get("IGNITE_PORT", "47500");
                addresses.add(address);
            } else {
                config.setClientMode(false);
                addresses.add("127.0.0.1:47500..47509");
            }

            ipFinder.setAddresses(addresses);
            discoSpi.setIpFinder(ipFinder);
            config.setDiscoverySpi(discoSpi);

            CacheConfiguration employeeCache = new CacheConfiguration<>("EmployeeCache").
                    setIndexedTypes(Integer.class, Employee.class);

            config.setCacheConfiguration(employeeCache);

            return Ignition.start(config);
        }

        return null;
    }
}
