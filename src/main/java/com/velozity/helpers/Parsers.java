package com.velozity.helpers;

import com.google.gson.Gson;
import com.velozity.vshop.Global;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
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

    public String locationToBase64(Location loc) {
        return Base64.getEncoder().encodeToString((loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch()).getBytes());
    }

    public Location base64ToLocation(String base64) {

        String decoded = new String(Base64.getDecoder().decode(base64));
        System.out.println(decoded);
        System.out.println(Arrays.toString(decoded.split(":")));
        World world = Bukkit.getWorld(decoded.split(":")[0]);
        double x = Double.parseDouble(decoded.split(":")[1]);
        double y = Double.parseDouble(decoded.split(":")[2]);
        double z = Double.parseDouble(decoded.split(":")[3]);
        float yaw = Float.parseFloat(decoded.split(":")[4]);
        float pitch = Float.parseFloat(decoded.split(":")[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }
}
