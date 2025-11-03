package com.zgamelogic.application.services;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class KubernetesService {
    private final Route53Service route53Service;
    private final KubernetesClient client;

    @PostConstruct
    public void init(){
//        client.pods().inAnyNamespace().list().getItems().forEach(pod -> {
//            System.out.println(pod.getMetadata().getName() + " " + pod.getStatus());
//        });
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
    public void updateServices() {
        client.network().v1().ingresses().inAnyNamespace().watch(new Watcher<>() {
            @Override
            public void eventReceived(Action action, Ingress ingress) {
                String label = ingress.getMetadata().getLabels().getOrDefault("route53", null);
                if (label == null) return;
                List<String> routes = route53Service.getCnameRecords().stream().map(s -> s.split("\\.")[0].toLowerCase()).toList();
                if (routes.contains(label.toLowerCase())) return;
                log.info("Creating route53 path for label {}", label);
                route53Service.addCnameRecord(label);
            }

            @Override
            public void onClose(WatcherException e) {
                log.error("Ingress watcher closed with error", e);

                try {
                    Thread.sleep(3000); // small backoff
                } catch (InterruptedException ignored) {}
                updateServices();
            }
        });
    }
}
