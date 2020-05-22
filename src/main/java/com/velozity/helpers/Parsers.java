package com.velozity.helpers;

import com.google.gson.Gson;
import com.velozity.vshop.Global;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Parsers {
    public Integer signPrice(String line) {

        String toParse = line.toLowerCase().replaceAll("[^\\.0123456789]","").trim();
        if(toParse.isEmpty()) {
            return -2;
        }

        int price = -1;
        try {
            price = Integer.parseInt(toParse);
        } catch (NumberFormatException e) {
            return -1;
        }

        return price;
    }

    public String locationToBase64(Location location) {
        return Base64.getEncoder().encodeToString(location.serialize().toString().getBytes());
    }

    public Location base64ToLocation(String base64) {
        return new Gson().fromJson(new String(Base64.getDecoder().decode(base64)), Location.class);
    }
}
