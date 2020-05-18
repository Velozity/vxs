package com.velozity.helpers;

import com.jasongoodwin.monads.Try;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

public class FileIO {
    private ExecutorService executor
            = Executors.newSingleThreadExecutor();

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

    public void writeYML(Map<String, Object> data, String path) throws IOException {

            if (!isWorkspaceReady()) {
                log.severe("VShop - Unable to setup file workspace, check file permissions or try restarting the server.");
                return;
            }

            Yaml yaml = new Yaml();
            FileWriter writer = new FileWriter(path);
            yaml.dump(data, writer);
            return;
    }
}

