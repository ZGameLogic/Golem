package com.zgamelogic.application.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("kubernetes")
public class KubernetesController {
    private final KubernetesService kubernetesService;

    @GetMapping("pods")
    public List<Pod> getPods() {
        return kubernetesService.getAllNodes();
    }
}
