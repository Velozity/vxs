package com.velozity.expansions;

import com.velozity.types.LogType;
import com.velozity.vshop.Global;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class VXChecksum {
    public VXChecksum(Plugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(Global.getMainInstance, () -> {
            String server = "https://vxdev.org/secure/checksum/version/" + Global.projectId;
            String current = plugin.getDescription().getVersion();

            try {
                URL check = new URL(server);
                HttpsURLConnection connection = (HttpsURLConnection) check.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.connect();

                if(connection.getResponseCode() == 404) {
                    return;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));

                String inputLine;
                StringBuilder res = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                    res.append(inputLine);

                if (!current.equals(res.toString())) {
                    Global.interact.logServer(LogType.info, "NEW VERSION DETECTED - Download at https://vxdev.org/projects/" + Global.projectId);
                }
                in.close();

                connection.disconnect();
            } catch (IOException ignored) {

            }
        });
    }
}

class Version implements Comparable<Version> {

    private String version;

    public final String get() {
        return this.version;
    }

    public Version(String version) {
        if(version == null)
            throw new IllegalArgumentException("Version can not be null");
        if(!version.matches("[0-9]+(\\.[0-9]+)*"))
            throw new IllegalArgumentException("Invalid version format");
        this.version = version;
    }

    @Override public int compareTo(Version that) {
        if(that == null)
            return 1;
        String[] thisParts = this.get().split("\\.");
        String[] thatParts = that.get().split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for(int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if(thisPart < thatPart)
                return -1;
            if(thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    @Override public boolean equals(Object that) {
        if(this == that)
            return true;
        if(that == null)
            return false;
        if(this.getClass() != that.getClass())
            return false;
        return this.compareTo((Version) that) == 0;
    }

}
