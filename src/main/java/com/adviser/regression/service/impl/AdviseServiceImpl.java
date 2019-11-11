package com.adviser.regression.service.impl;

import com.adviser.regression.model.Advise;
import com.adviser.regression.model.TickData;
import com.adviser.regression.service.AdviseService;
import com.adviser.regression.service.Adviser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class AdviseServiceImpl implements AdviseService {

    @Autowired
    private Adviser adviser;

    @Override
    public Mono<Advise> getAdvise(String currency) {
        Advise data = adviser.getAdvise(currency);
        return Mono.just(data);
    }

    @Override
    public void addTickData(TickData tickData) {
        try {
            adviser.addTickData(tickData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
