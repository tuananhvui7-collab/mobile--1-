package com.ecommerce.mobile.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.mobile.service.ShipmentService;

@RestController
@RequestMapping("/webhooks/ghn")
public class GhnWebhookController {

    private final ShipmentService shipmentService;

    public GhnWebhookController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping(value = "/order-status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> orderStatus(@RequestBody Map<String, Object> payload) {
        shipmentService.processGhnWebhook(payload);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("message", "Success");
        return response;
    }
}
