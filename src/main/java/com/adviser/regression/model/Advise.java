package com.adviser.regression.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Advise {
    private OrderType orderType;
    private int closedTick;
    private float openPrice;
    private float closePrice;
}
