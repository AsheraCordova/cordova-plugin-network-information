package org.apache.cordova.networkinformation;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkMonitor {

    public interface NetworkChangeListener {
        void checkNetwork();
    }

    private final List<NetworkChangeListener> listeners = new CopyOnWriteArrayList<>();
    private boolean running = false;

    public void addListener(NetworkChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NetworkChangeListener listener) {
        listeners.remove(listener);
    }

    public void startMonitoring(long intervalMs) {
        if (running) return;
        running = true;

        Thread monitorThread = new Thread(() -> {
            while (running) {
                try {
                    for (NetworkChangeListener l : listeners) {
                        l.checkNetwork();
                    }
                    Thread.sleep(intervalMs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    public void stopMonitoring() {
        running = false;
    }

}
