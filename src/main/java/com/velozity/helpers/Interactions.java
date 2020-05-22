package com.velozity.helpers;

import com.google.common.collect.ObjectArrays;
import com.velozity.types.LogType;
import com.velozity.vshop.Global;
import com.velozity.vshop.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Interactions {

    public void logServer(LogType type, String msg) {
        String logType = "";

        switch(type) {
            case info:
                logType = "INFO: ";
                break;
            case error:
                logType = "ERROR: ";
                break;
            case warning:
                logType = "WARNING: ";
                break;
            default:
                logType = "UNKNOWN: ";
                break;
        }

        Global.getMainInstance.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[VShop] " + logType + msg);

        if((Boolean) Global.mainConfig.readSetting("system", "filelogging")) {
            LocalDate date = LocalDate.now();
            DateTimeFormatter dateF = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            LocalTime time = LocalTime.now();
            DateTimeFormatter timeF = DateTimeFormatter.ofPattern("HH:mm:ss");

            String timestamp = "[" + date.format(dateF) + " " + time.format(timeF) + "] ";
            writeToLogFile(timestamp + logType + msg);
        }
    }

    private void writeToLogFile(String msg) {
        try {
            File file = new File("plugins/VShop/Logs", "log.txt");

            if(!file.exists()) {
                if(file.getParentFile().mkdir())
                file.createNewFile();
            }

            if(file.length() > ((Integer)Global.mainConfig.readSetting("system", "maxloggingsize") * 1e+6)) {
                LocalDate date = LocalDate.now();
                DateTimeFormatter dateF = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                FileOutputStream fos = new FileOutputStream("plugins/VShop/Logs/Logs-" + date.format(dateF) + ".zip");
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                FileInputStream fis = new FileInputStream(file);
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                zipOut.close();
                fis.close();
                fos.close();
                file.delete();
            }

            BufferedWriter writer = new BufferedWriter(
                    new FileWriter("plugins/VShop/Logs/log.txt", true)  //Set true for append mode
            );
            writer.write(msg);
            writer.newLine();   //Add new line
            writer.close();

        } catch (IOException ex) {
            Global.getMainInstance.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[VShop] " + "UNABLE TO WRITE TO LOG FILE! PLEASE DISABLE FILE LOGGING OR FIX FILE PERMISSIONS.");
        }
    }

    public void msgPlayer(String msg, Player player) {
        player.sendMessage("§4[VShop]§r " + msg);
    }

    public void msgPlayer(String[] msg, Player player) {
        player.sendMessage(ObjectArrays.concat("§4[VShop]§r", msg));
    }

}
