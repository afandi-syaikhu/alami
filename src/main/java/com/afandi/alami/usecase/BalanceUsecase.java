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

            // to skip first line
            br.readLine();

            int chunkIndex = 0, chunkSize = (int) Math.ceil((float) avgBalanceTotalRow / avgBalanceTotalThread);
            Map<Integer, Map<String, Map<Integer, String>>> rowsChunk = new HashMap<>();

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(PunctuationConstant.SEMICOLON);

                String id = values[IndexConstant.EOD_BEFORE_ID];
                int balance = Integer.parseInt(values[IndexConstant.EOD_BEFORE_BALANCE]);
                int prevBalance = Integer.parseInt(values[IndexConstant.EOD_BEFORE_PREV_BALANCE]);
                float avgBalance = (float) (balance + prevBalance) / 2;

                Map<Integer, String> cells = new HashMap<>();
                cells.put(IndexConstant.EOD_AFTER_AVG_BALANCE, String.format("%.2f", avgBalance));

                Map<String, Map<Integer, String>> row = new HashMap<>();
                row.put(id, cells);

                // put into first ever chunk
                Map<String, Map<Integer, String>> chunk = rowsChunk.get(chunkIndex);
                if (chunk == null || chunk.isEmpty()) {
                    rowsChunk.put(chunkIndex, row);
                    continue;
                }

                // put into existing chunk
                if (chunk.size() < chunkSize) {
                    chunk.put(id, cells);
                    rowsChunk.put(chunkIndex, chunk);
                    continue;
                }

                // create new chunk when the rest is full
                rowsChunk.put(++chunkIndex, row);

            }

            br.close();

            rowsChunk.forEach((key, val) -> {
                RowModifier dataRows = new RowModifier();
                dataRows.setDir(eodDir);
                dataRows.setFileName(DocumentConstant.EOD_AFTER_NAME);
                dataRows.setRows(val);

                ModifyFileThread worker = new ModifyFileThread(dataRows);

                worker.start();
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    System.out.printf("[%s] => %s\n", getClass().getName(), e.getMessage());
                }

            });

            System.out.println("== Calculate Average Balance Has Finished ==");

        } catch (IOException e) {
            System.out.printf("[%s] => %s\n", getClass().getName(), e.getMessage());
        }
    }
}
