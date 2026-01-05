package com.banking.analytics.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class InfluxDBService {

    private final InfluxDBClient influxDBClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String organization;

    public void writeTransactionMetric(String userId, BigDecimal amount, String type, String status) {
        try {
            Point point = Point.measurement("transactions")
                    .addTag("user_id", userId)
                    .addTag("type", type)
                    .addTag("status", status)
                    .addField("amount", amount.doubleValue())
                    .time(Instant.now(), WritePrecision.NS);

            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            writeApi.writePoint(bucket, organization, point);

        } catch (Exception e) {
            log.error("Error writing transaction metric to InfluxDB: {}", e.getMessage());
        }
    }

    public void writeBalanceMetric(String userId, String accountId, BigDecimal balance) {
        try {
            Point point = Point.measurement("account_balance")
                    .addTag("user_id", userId)
                    .addTag("account_id", accountId)
                    .addField("balance", balance.doubleValue())
                    .time(Instant.now(), WritePrecision.NS);

            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            writeApi.writePoint(bucket, organization, point);

        } catch (Exception e) {
            log.error("Error writing balance metric to InfluxDB: {}", e.getMessage());
        }
    }

    public void writeAccountCreationEvent(String userId, String accountId) {
        try {
            Point point = Point.measurement("account_events")
                    .addTag("user_id", userId)
                    .addTag("account_id", accountId)
                    .addTag("event_type", "CREATED")
                    .addField("count", 1)
                    .time(Instant.now(), WritePrecision.NS);

            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            writeApi.writePoint(bucket, organization, point);

        } catch (Exception e) {
            log.error("Error writing account creation event to InfluxDB: {}", e.getMessage());
        }
    }
}