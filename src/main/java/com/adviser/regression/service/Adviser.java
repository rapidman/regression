package com.adviser.regression.service;

import com.adviser.regression.model.Advise;
import com.adviser.regression.model.OrderType;
import com.adviser.regression.model.TickData;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.adviser.regression.Constants.FUNCTION_OFFSET;
import static com.adviser.regression.Constants.REGRESSION_LINE_COUNT;

@Component
public class Adviser {
    public static final String REAL_PRICE = "Real price";

    private Map<String, LinkedList<TickData>> ticks = new ConcurrentHashMap<>();

    public Advise getAdvise(String currency) {
        if (ticks.size() < 1 || ticks.get(currency).size() < FUNCTION_OFFSET){
            return Advise.builder()
                    .orderType(OrderType.NONE)
                    .build();
        }

        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        XYSeries series = new XYSeries(REAL_PRICE);

        LinkedList<TickData> tickDataList = ticks.get(currency);
        for (TickData tickData : tickDataList.subList(tickDataList.size() - REGRESSION_LINE_COUNT, tickDataList.size())) {
            series.add(tickData.getTickNumber(), tickData.getPrice());
        }
        seriesCollection.addSeries(series);
        double[] regressionParameters = Regression.getOLSRegression(seriesCollection, 0);

        LineFunction2D linefunction2d = new LineFunction2D(
                regressionParameters[0], regressionParameters[1]);


        int start = tickDataList.get(tickDataList.size() > FUNCTION_OFFSET ? tickDataList.size() - FUNCTION_OFFSET - 1
                : tickDataList.size() - REGRESSION_LINE_COUNT).getTickNumber();
        XYDataset dataset = DatasetUtilities.sampleFunction2D(linefunction2d,
                start, tickDataList.getLast().getTickNumber(), 200, "");
        float openPrice = dataset.getY(0, 0).floatValue();
        float closePrice = dataset.getY(0, 1).floatValue();
        boolean up = openPrice < closePrice;
        return Advise.builder()
                .closedTick(tickDataList.getLast().getTickNumber())
                .closePrice(closePrice)
                .openPrice(openPrice)
                .orderType(up ? OrderType.BUY : OrderType.SELL)
                .build();
    }

    public void addTickData(TickData tickData) {
        ticks.computeIfAbsent(tickData.getCurrency(), s -> new LinkedList<>())
                .add(tickData);
        int size = ticks.get(tickData.getCurrency()).size();
        if(size % 1000 == 0){
            System.out.println("ticks size:"+ size);
        }
    }
}
