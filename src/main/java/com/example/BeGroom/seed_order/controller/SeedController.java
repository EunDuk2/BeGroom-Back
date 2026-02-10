package com.example.BeGroom.seed_order.controller;

import com.example.BeGroom.seed_order.dto.SeedInitRequest;
import com.example.BeGroom.seed_order.dto.SeedInitResponse;
import com.example.BeGroom.seed_order.dto.SeedRunRequest;
import com.example.BeGroom.seed_order.dto.SeedRunResponse;
import com.example.BeGroom.seed_order.service.SeedInitService;
import com.example.BeGroom.seed_order.service.SeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seed")
public class SeedController {

    private final SeedInitService seedInitService;

    @PostMapping("/init")
    public SeedInitResponse init(@RequestBody SeedInitRequest req) {
        return seedInitService.init(req);
    }

    /**
     * orders.csv로 그대로 붙여넣기 위한 엔드포인트
     */
    @PostMapping(value = "/init/orders-csv", produces = "text/plain; charset=UTF-8")
    public String initOrdersCsv(@RequestBody SeedInitRequest req) {
        return seedInitService.initAsOrdersCsv(req);
    }
}

