package com.velozity.helpers;

import com.velozity.types.Shop;

import java.io.*;
import java.util.logging.Logger;

public class FileIO {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String workPath = "plugins/VShop";

    public Boolean setupWorkspace() throws IOException {

            File dirPath = new File(workPath + "/");
            if(!dirPath.exists()) {

                dirPath.mkdir();
            }

            File shopPath = new File(workPath + "/shops.yml");

            if(!shopPath.exists()) {
                if (!shopPath.createNewFile()) {
                    return false;
                }
            }

            return true;
    }

    public boolean isWorkspaceReady() {
        File file = new File("plugins/VShop/");
        if (!file.exists()) {
            return false;
        }

        return true;
    }

    public void writeShop(Shop shop) throws IOException {


    }

    public void getShops() {

    }
}

