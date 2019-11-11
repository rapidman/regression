package com.adviser.regression.model;

import lombok.Data;

@Data
public class TickData {
    private String currency;
    private int tickNumber;
    private float price;
}
