package com.afandi.alami.delivery.cli;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.afandi.alami.usecase.BalanceUsecase;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "eod", mixinStandardHelpOptions = true, description = "Execute end of day procedure")
public class EndOfDayCommand implements Callable<Integer> {

    @Option(names = { "-n", "--number" }, description = "number of questions to be demonstrated")
    private Integer n;

    @Autowired
    private BalanceUsecase balanceUC;

    @Override
    public Integer call() throws Exception {

        if (n == null) {
            balanceUC.calculateAverageBalance();
        } else if (n == 1) {
            balanceUC.calculateAverageBalance();
        } else {
            System.out.println("== Invalid Argument ==");
        }

        return 0;
    }
}
