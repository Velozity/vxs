package com.velozity.types;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Shop implements ConfigurationSerializable {
    @Getter
    @Setter
    public String title;
    @Getter
    @Setter
    public ItemStack item;
    @Getter
    @Setter
    public Integer buyprice = 0;
    @Getter
    @Setter
    public Integer sellprice = 0;
    @Getter
    @Setter
    public Boolean buyable = true, sellable = true;

    public Shop(String title, ItemStack item, Integer buyprice, Integer sellprice, Boolean buyable, Boolean sellable) {
        this.title = title;
        this.item = item;
        this.buyprice = buyprice;
        this.sellprice = sellprice;
        this.buyable = buyable;
        this.sellable = sellable;
    }

    public Shop() {

    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", title);
        data.put("item", item);
        data.put("buyprice", buyprice);
        data.put("sellprice", sellprice);
        data.put("buyable", buyable);
        data.put("sellable", sellable);

        return data;
    }
}
