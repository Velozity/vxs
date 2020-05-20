package com.velozity.types;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Shop implements ConfigurationSerializable {
    @Getter
    @Setter
    public String title, itemid;
    @Getter
    @Setter
    public List<String> lore;
    @Getter
    @Setter
    public Integer buyprice = 0;
    @Getter
    @Setter
    public Integer sellprice = 0;
    @Getter
    @Setter
    public Boolean buyable = true, sellable = true;
    @Getter
    @Setter
    public List<PotionEffect> potiondata;


    public Shop(String title, String itemid, List<String> lore, Integer buyprice, Integer sellprice, Boolean buyable, Boolean sellable, List<PotionEffect> potiondata) {
        this.title = title;
        this.itemid = itemid;
        this.lore = lore;
        this.buyprice = buyprice;
        this.sellprice = sellprice;
        this.buyable = buyable;
        this.sellable = sellable;
        this.potiondata = potiondata;
    }

    public Shop() {

    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", title);
        data.put("itemid", itemid);
        data.put("lore", lore);
        data.put("buyprice", buyprice);
        data.put("sellprice", sellprice);
        data.put("buyable", buyable);
        data.put("sellable", sellable);
        data.put("potiondata", potiondata);

        return data;
    }

    @Override
    public String toString() {
        return "title: " + title + ", itemid: " + itemid + ", lore:" + lore + ", buyprice: " + buyprice + ", sellprice: " + sellprice + ", buyable: " + buyable + ", sellable: " + sellable;
    }
}
