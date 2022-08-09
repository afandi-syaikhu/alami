package com.afandi.alami.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RowModifier {
    private int chunkSize;
    private String filePath;
    private Map<String, Map<Integer, String>> rows = new HashMap<>();
}
