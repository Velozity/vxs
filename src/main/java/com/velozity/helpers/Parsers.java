package com.velozity.helpers;

import com.velozity.vshop.Global;

public class Parsers {
    public Integer signPrice(String line) {
        Global.log.info("25");
        String toParse = line.replace("B", "").replace("$", "").replace("-", "").trim();
        Global.log.info("2");
        if(!toParse.isEmpty()) {
            return Integer.parseInt(toParse);
        }

        return -1;
    }
}
