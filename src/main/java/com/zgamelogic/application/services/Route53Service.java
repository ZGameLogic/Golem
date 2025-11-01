package com.zgamelogic.application.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

@Slf4j
@Service
@AllArgsConstructor
public class Route53Service {
    private final Route53Client route53Client;

    @Scheduled(cron = "0 * * * * *")
    private void dynamicIpUpdate() throws IOException {
        String hostedZoneId = "Z10458543LUY6QMAE4J12";
        String recordName = "zgamelogic.com";

        // 1. Get current public IP
        String currentIp;
        Scanner s = new Scanner(new URL("https://checkip.amazonaws.com").openStream(), StandardCharsets.UTF_8);
        currentIp = s.next().trim();

        // 2. Fetch current Route 53 record
        ListResourceRecordSetsResponse response = route53Client.listResourceRecordSets(
            ListResourceRecordSetsRequest.builder()
                .hostedZoneId(hostedZoneId)
                .startRecordName(recordName)
                .startRecordType(RRType.A)
                .maxItems("1")
                .build()
        );

        String existingIp = null;
        if (!response.resourceRecordSets().isEmpty()
            && response.resourceRecordSets().get(0).type() == RRType.A) {
            existingIp = response.resourceRecordSets().get(0).resourceRecords().get(0).value();
        }

        // 3. Compare and update if changed
        if (!currentIp.equals(existingIp)) {
            ResourceRecordSet recordSet = ResourceRecordSet.builder()
                .name(recordName)
                .type(RRType.A)
                .ttl(300L)
                .resourceRecords(ResourceRecord.builder().value(currentIp).build())
                .build();

            Change change = Change.builder()
                .action(ChangeAction.UPSERT)
                .resourceRecordSet(recordSet)
                .build();

            ChangeBatch changeBatch = ChangeBatch.builder()
                .changes(change)
                .build();

            ChangeResourceRecordSetsRequest updateRequest = ChangeResourceRecordSetsRequest.builder()
                .hostedZoneId(hostedZoneId)
                .changeBatch(changeBatch)
                .build();

            route53Client.changeResourceRecordSets(updateRequest);

            log.info("Updated A record to new IP: {}", currentIp);
        }
    }

    public List<String> getCnameRecords() {
        String hostedZoneId = "Z10458543LUY6QMAE4J12";
        List<String> results = new java.util.ArrayList<>();

        String nextName = null;
        RRType nextType = null;
        boolean truncated;

        do {
            ListResourceRecordSetsRequest.Builder reqBuilder = ListResourceRecordSetsRequest.builder()
                .hostedZoneId(hostedZoneId)
                .maxItems("100");

            if (nextName != null) {
                reqBuilder.startRecordName(nextName).startRecordType(nextType);
            }

            ListResourceRecordSetsResponse resp = route53Client.listResourceRecordSets(reqBuilder.build());

            for (ResourceRecordSet rrs : resp.resourceRecordSets()) {
                if (rrs.type() == RRType.CNAME) {
                    String name = rrs.name();
                    results.add(name);
                }
            }

            truncated = resp.isTruncated();
            if (truncated) {
                nextName = resp.nextRecordName();
                nextType = resp.nextRecordType();
            } else {
                nextName = null;
                nextType = null;
            }
        } while (truncated);

        return results;
    }

    public void addCnameRecord(String prefix) {
        String hostedZoneId = "Z10458543LUY6QMAE4J12";

        ResourceRecordSet recordSet = ResourceRecordSet.builder()
            .name(prefix + ".zgamelogic.com")
            .type(RRType.CNAME)
            .ttl(300L)
            .resourceRecords(ResourceRecord.builder().value("zgamelogic.com").build())
            .build();

        Change change = Change.builder()
            .action(ChangeAction.UPSERT)
            .resourceRecordSet(recordSet)
            .build();

        ChangeBatch changeBatch = ChangeBatch.builder()
            .changes(change)
            .build();

        ChangeResourceRecordSetsRequest request = ChangeResourceRecordSetsRequest.builder()
            .hostedZoneId(hostedZoneId)
            .changeBatch(changeBatch)
            .build();

        route53Client.changeResourceRecordSets(request);
    }
}
