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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.adviser.regression.Constants.FUNCTION_OFFSET;
import static com.adviser.regression.Constants.REGRESSION_LINE_COUNT;

@Component
public class Adviser {
    public static final String REAL_PRICE = "Real price";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final String TMP_FOREX_TICKS_FILE = "/tmp/forex_ticks.txt";
    private AtomicInteger count = new AtomicInteger(0);
    private Object monitor = new Object();

    private Map<String, LinkedList<TickData>> ticks = new ConcurrentHashMap<>();

    public Advise getAdvise(String currency) {
        int adviseCount = count.incrementAndGet();
//        if (ticks.size() < 1 || ticks.get(currency).size() < FUNCTION_OFFSET || adviseCount % REGRESSION_LINE_COUNT != 0){
        synchronized (monitor) {
            if (ticks.size() < 1 || ticks.get(currency).size() < FUNCTION_OFFSET) {
                return Advise.builder()
                        .orderType(OrderType.NONE)
                        .build();
            }
        }

        LinkedList<TickData> tickDataList = ticks.get(currency);
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        XYSeries series = new XYSeries(REAL_PRICE);

        int size = tickDataList.size();
        synchronized (monitor) {
            List<TickData> subList = tickDataList.subList(size - FUNCTION_OFFSET, size);
            for (TickData tickData : subList) {
                series.add(tickData.getTickNumber(), tickData.getPrice());
            }
        }
        seriesCollection.addSeries(series);
        double[] regressionParameters = Regression.getOLSRegression(seriesCollection, 0);

        LineFunction2D linefunction2d = new LineFunction2D(
                regressionParameters[0], regressionParameters[1]);


        int start;
        int tickNumber;
        synchronized (monitor) {
            start = tickDataList.get(size > FUNCTION_OFFSET ? size - FUNCTION_OFFSET - 1
                    : size - REGRESSION_LINE_COUNT).getTickNumber();
            tickNumber = tickDataList.getLast().getTickNumber();
        }
        XYDataset dataset = DatasetUtilities.sampleFunction2D(linefunction2d,
                start, tickNumber, 200, "");
        float openPrice = dataset.getY(0, 0).floatValue();
        float closePrice = dataset.getY(0, 1).floatValue();
        boolean up = openPrice < closePrice;
        return Advise.builder()
                .closedTick(tickNumber)
                .closePrice(closePrice)
                .openPrice(openPrice)
                .orderType(up ? OrderType.BUY : OrderType.SELL)
                .build();
    }

    public void addTickData(TickData tickData) throws IOException {
        synchronized (monitor) {
            ticks.computeIfAbsent(tickData.getCurrency(), s -> new LinkedList<>())
                    .add(tickData);
            int size = ticks.get(tickData.getCurrency()).size();
            if (size % 1000 == 0) {
                System.out.println("ticks size:" + size);
            }
        }
        File file = new File(TMP_FOREX_TICKS_FILE);
        if (!file.exists()) {
            file.createNewFile();
        }
        try (PrintWriter output = new PrintWriter(new FileWriter(file, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(SIMPLE_DATE_FORMAT.format(new Date()))
                    .append(" ")
                    .append(tickData.getCurrency())
                    .append(" ")
                    .append(tickData.getTickNumber())
                    .append(" ")
                    .append(tickData.getPrice());
            output.printf("%s\r\n", sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
