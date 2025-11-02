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
    public void init(){
        KubernetesClient client = new KubernetesClientBuilder().build();
        client.nodes().list().getItems().forEach(node -> {
            System.out.println("Name: " + node.getMetadata().getName());
            System.out.println("Labels: " + node.getMetadata().getLabels());
            System.out.println("Capacity: " + node.getStatus().getCapacity());
            System.out.println("Allocatable: " + node.getStatus().getAllocatable());
            System.out.println("Conditions: " + node.getStatus().getConditions());
            System.out.println("Addresses: " + node.getStatus().getAddresses());
            System.out.println("Kubelet Version: " + node.getStatus().getNodeInfo().getKubeletVersion());
            System.out.println("-----------------------------------");
        });
    }

    @PostConstruct
    @Scheduled(cron = "0 */15 * * * *")
    public void updateServices() {
        List<String> routes = route53Service.getCnameRecords().stream().map(s -> s.split("\\.")[0].toLowerCase()).toList();
        KubernetesClient client = new KubernetesClientBuilder().build();
        client.network().v1().ingresses().inAnyNamespace().list().getItems().forEach(service -> {
            String label = service.getMetadata().getLabels().getOrDefault("route53", null);
            if(label == null) return;
            if(routes.contains(label.toLowerCase())) return;
            route53Service.addCnameRecord(label);
        });
    }
}
