package com.velozity.helpers;

public class Parsers {
    public Integer signPrice(String line) {

        String toParse = line.replace("B", "").replace("$", "").replace("-", "").trim();

        if(!toParse.isEmpty()) {
            return Integer.parseInt(toParse);
        }

        return -1;
    }
}
