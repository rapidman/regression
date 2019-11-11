package com.adviser.regression.controller;

import com.adviser.regression.model.Advise;
import com.adviser.regression.model.TickData;
import com.adviser.regression.service.AdviseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/adviser")
public class AdviserController {
    @Autowired
    private AdviseService adviseService;

    @GetMapping("/{currency}")
    public Mono<Advise> getAdvise(@PathVariable String currency){
        return adviseService.getAdvise(currency);
    }

    @PostMapping
    public void addTicks(@RequestBody TickData tickData){
        adviseService.addTickData(tickData);
    }

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            TickData tickData = objectMapper.readValue("{\"currency\":\"usd\", \"xTickNumber\":45553400, \"yPrice\":1.09756}", TickData.class);
            System.out.println(tickData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
