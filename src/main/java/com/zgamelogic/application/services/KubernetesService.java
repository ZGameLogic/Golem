package com.zgamelogic.application.services;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class KubernetesService {
    private final Route53Service route53Service;

    @PostConstruct
    @Scheduled(cron = "0 */15 * * * *")
    public void updateServices() {
        List<String> routes = route53Service.getCnameRecords().stream().map(s -> s.split("\\.")[0].toLowerCase()).toList();
        KubernetesClient client = new KubernetesClientBuilder().build();
        client.network().v1().ingresses().inNamespace("default").list().getItems().forEach(service -> {
            String label = service.getMetadata().getLabels().getOrDefault("route53", null);
            if(label == null) return;
            if(routes.contains(label.toLowerCase())) return;
            route53Service.addCnameRecord(label);
        });
    }
}
