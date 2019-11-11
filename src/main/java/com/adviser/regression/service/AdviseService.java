package com.adviser.regression.service;

import com.adviser.regression.model.Advise;
import com.adviser.regression.model.TickData;
import reactor.core.publisher.Mono;

public interface AdviseService {
    Mono<Advise> getAdvise(String currency);

    void addTickData(TickData tickData);
}
