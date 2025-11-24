package com.Lino.dupeGuard.service;

import com.Lino.dupeGuard.DupeGuard;
import org.bukkit.Bukkit;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogService {
    private final File logFile;
    private final BlockingQueue<String> queue;
    private volatile boolean running;

    public LogService(DupeGuard plugin) {
        this.logFile = new File(plugin.getDataFolder(), "bans.log");
        this.queue = new LinkedBlockingQueue<>();
        this.running = true;

        if (!logFile.exists()) {
            try { logFile.createNewFile(); } catch (IOException ignored) {}
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::processQueue);
    }

    private void processQueue() {
        while (running || !queue.isEmpty()) {
            try {
                String entry = queue.take();
                try (FileWriter fw = new FileWriter(logFile, true);
                     BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(entry);
                    bw.newLine();
                }
            } catch (InterruptedException | IOException e) {
                if (!running) break;
            }
        }
    }

    public void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        queue.offer("[" + timestamp + "] " + message);
    }

    public void shutdown() {
        running = false;
        log("Plugin disabling, flushing logs...");
    }
}