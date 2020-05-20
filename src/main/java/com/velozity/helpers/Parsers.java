package com.velozity.helpers;

import com.velozity.vshop.Global;

public class Parsers {
    public Integer signPrice(String line) {

        String toParse = line.toLowerCase().replace("b", "").replace("s", "").replace("$", "").replace("-", "").replace(" ", "").trim();
        int price = -1;
        try {
            price = Integer.parseInt(toParse);
        } catch (NumberFormatException e) {
            return -1;
        }

        return price;
    }
}
