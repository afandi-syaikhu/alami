package com.afandi.alami.usecase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.afandi.alami.constant.DocumentConstant;
import com.afandi.alami.constant.IndexConstant;
import com.afandi.alami.constant.PunctuationConstant;
import com.afandi.alami.model.RowModifier;
import com.afandi.alami.thread.ModifyFileThread;

@Component
public class BalanceUsecase {

    @Value("${eod.docs.location}")
    private String eodDir;

    @Value("${eod.docs.avg-balance.total-row}")
    private Integer avgBalanceTotalRow;

    @Value("${eod.docs.avg-balance.total-thread}")
    private Integer avgBalanceTotalThread;

    public void calculateAverageBalance() {
        try {
            String eodLocation = eodDir + DocumentConstant.EOD_BEFORE_NAME;
            FileReader fr = new FileReader(eodLocation);
            BufferedReader br = new BufferedReader(fr);

            // skip the header / first row
            br.readLine();

            Map<String, Map<Integer, String>> rows = new HashMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(PunctuationConstant.SEMICOLON);

                String id = values[IndexConstant.EOD_BEFORE_ID];
                int balance = Integer.parseInt(values[IndexConstant.EOD_BEFORE_BALANCE]);
                int prevBalance = Integer.parseInt(values[IndexConstant.EOD_BEFORE_PREV_BALANCE]);
                float avgBalance = (float) (balance + prevBalance) / 2;

                Map<Integer, String> cells = new HashMap<>();
                cells.put(IndexConstant.EOD_AFTER_AVG_BALANCE, String.format("%.2f", avgBalance));

                rows.put(id, cells);
            }

            br.close();

            RowModifier rowModifier = new RowModifier();
            rowModifier.setFilePath(eodDir + DocumentConstant.EOD_AFTER_NAME);
            rowModifier.setChunkSize((int) Math.ceil((float) avgBalanceTotalRow / avgBalanceTotalThread));
            rowModifier.setRows(rows);

            for (int i = 0; i < avgBalanceTotalThread; i++) {
                ModifyFileThread thread = new ModifyFileThread(rowModifier);
                thread.start();
            }

            System.out.println("== Calculate Average Balance Has Finished ==");

        } catch (IOException e) {
            System.out.printf("[%s] => %s\n", getClass().getName(), e.getMessage());
        }
    }
}
