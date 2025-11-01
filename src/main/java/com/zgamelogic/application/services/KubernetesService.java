package com.zgamelogic.application.services;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class KubernetesService {

    @PostConstruct
    @Scheduled(cron = "0 * * * * *")
    public void init() {
        KubernetesClient client = new KubernetesClientBuilder().build();
        PodList podList = client.pods().inNamespace("default").list();
        System.out.println("=== Current Pods in Cluster ===");
        for (Pod pod : podList.getItems()) {
            String name = pod.getMetadata().getName();
            String phase = pod.getStatus().getPhase();
            System.out.printf("%-38s %s%n", name, phase);
        }
    }
}
