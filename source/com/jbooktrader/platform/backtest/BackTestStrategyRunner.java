package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.util.*;

/**
 * Runs a trading strategy in the backtesting mode using a file containing
 * historical market data.
 */
public class BackTestStrategyRunner implements Runnable {
    private final BackTestDialog backTestDialog;
    private final Strategy strategy;
    private BackTestFileReader backTestFileReader;
    private BackTester backTester;

    public BackTestStrategyRunner(BackTestDialog backTestDialog, Strategy strategy) {
        this.backTestDialog = backTestDialog;
        this.strategy = strategy;
        Dispatcher.getInstance().getTrader().getAssistant().addStrategy(strategy);
    }

    public void cancel() {
        backTestFileReader.cancel();
        if (backTester != null) {
            backTester.cancel();
        }
        backTestDialog.showProgress("Stopping back test...");
    }

    public void run() {
        try {
            backTestDialog.enableProgress();
            List<Indicator> indicators = strategy.getIndicatorManager().getIndicators();
            BarSize barSize = backTestDialog.getBarSize();
            strategy.getPerformanceManager().createPerformanceChartData(barSize, indicators);
            backTestFileReader = new BackTestFileReader(backTestDialog.getFileName());
            backTestFileReader.setFilter(backTestDialog.getDateFilter());
            backTestDialog.showProgress("Scanning historical data file...");
            backTestFileReader.scan();

            backTestDialog.showProgress("Running back test...");
            backTester = new BackTester(strategy, backTestFileReader, backTestDialog);
            backTester.execute();
        } catch (Throwable t) {
            MessageDialog.showError(t);
        } finally {
            backTestDialog.dispose();
        }
    }
}
