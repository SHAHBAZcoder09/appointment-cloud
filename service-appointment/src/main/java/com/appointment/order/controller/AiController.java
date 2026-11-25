package com.appointment.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.appointment.common.entity.DoctorSchedule;
import com.appointment.common.entity.Result;
import com.appointment.order.client.ScheduleClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/appointment/ai")
public class AiController {

    @Resource
    private ScheduleClient scheduleClient;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @PostMapping("/recommend")
    @SentinelResource(value = "aiRecommend", fallback = "aiFallback")
    public Result<Object> recommend(@RequestBody Map<String, String> body) throws Exception {
        String symptoms = body.getOrDefault("symptoms", "").trim();
        if (symptoms.isEmpty()) {
            return Result.fail(400, "Please describe your symptoms");
        }

        String apiKey = System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("DEEPSEEK_API_KEY not configured");
        }

        Result<List<DoctorSchedule>> schedRes = scheduleClient.findAll();
        String doctors = (schedRes != null && schedRes.getData() != null)
                ? schedRes.getData().stream()
                    .map(s -> s.getDoctorName() + " (" + s.getDepartment() + ", " + s.getAvailableQuota() + " slots)")
                    .collect(Collectors.joining("; "))
                : "Dr. Malik (Cardiology), Dr. Sharry (Dermatology), Dr. Nasir (Pediatrics)";

        String prompt = "You are a helpful clinic triage assistant at N4 Clinic. Available doctors: [" + doctors + "]. "
                + "Patient symptoms: \"" + symptoms + "\". "
                + "Recommend the best matching department and doctor in 2 short sentences. Be warm and clear.";

        String payload = mapper.writeValueAsString(Map.of(
                "model", "deepseek-chat",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 120
        ));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.deepseek.com/chat/completions"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("DeepSeek API error HTTP " + resp.statusCode());
        }

        JsonNode root = mapper.readTree(resp.body());
        String reply = root.path("choices").get(0).path("message").path("content").asText();
        return Result.ok(Map.of("advice", reply, "source", "DeepSeek AI"));
    }

    public Result<Object> aiFallback(Map<String, String> body, Throwable ex) {
        return Result.ok(Map.of(
                "advice", "AI triage is currently busy (Sentinel fallback). For heart/chest issues pick Cardiology (Dr. Malik); for skin issues pick Dermatology (Dr. Sharry); for child care pick Pediatrics (Dr. Nasir).",
                "source", "Sentinel Fallback"
        ));
    }
}
