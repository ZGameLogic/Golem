package com.zgamelogic.application.kubernetes;

import com.zgamelogic.application.services.Route53Service;
import io.fabric8.kubernetes.api.model.Pod;
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

    public List<Pod> getAllNodes(){
        return client.pods().inNamespace("default").list().getItems();
    }
}
