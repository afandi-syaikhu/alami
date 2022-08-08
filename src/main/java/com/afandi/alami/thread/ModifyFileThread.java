package com.afandi.alami.thread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.afandi.alami.constant.IndexConstant;
import com.afandi.alami.constant.PunctuationConstant;
import com.afandi.alami.model.RowModifier;
import com.opencsv.CSVWriter;

public class ModifyFileThread extends Thread {

    private RowModifier rowModifier;

    public ModifyFileThread(RowModifier r) {
        this.rowModifier = r;
    }

    @Override
    public void run() {
        try {
            String eodLocation = rowModifier.getDir() + rowModifier.getFileName();
            FileReader fr = new FileReader(eodLocation);

            List<String[]> contents = new ArrayList<>();
            Map<String, Map<Integer, String>> rows = rowModifier.getRows();
            long threadId = Thread.currentThread().getId();

            String line;
            BufferedReader reader = new BufferedReader(fr);
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(PunctuationConstant.SEMICOLON);

                String id = values[IndexConstant.EOD_AFTER_ID];
                Map<Integer, String> row = rows.get(id);
                if (row != null && !row.isEmpty()) {

                    // handle average balance
                    String cellAvgBalance = row.get(IndexConstant.EOD_AFTER_AVG_BALANCE);
                    if (cellAvgBalance != null && !cellAvgBalance.isEmpty()) {
                        values[IndexConstant.EOD_AFTER_AVG_BALANCE] = cellAvgBalance;
                        values[IndexConstant.EOD_AFTER_THREAD_NO_1] = String.valueOf(threadId);
                    }

                    contents.add(values);
                    continue;
                }

                contents.add(values);
            }

            reader.close();

            FileWriter fw = new FileWriter(eodLocation);
            CSVWriter writer = new CSVWriter(fw, PunctuationConstant.SEMICOLON.charAt(0),
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            writer.writeAll(contents);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            System.out.printf("[%s] => %s\n", getClass().getName(), e.getMessage());
        }
    }
}