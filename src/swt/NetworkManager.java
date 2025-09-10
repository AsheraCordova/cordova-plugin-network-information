package org.apache.cordova.networkinformation;

import java.net.InetAddress;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

public class NetworkManager extends CordovaPlugin {

    private static final String LOG_TAG = "NetworkManager";

    private CallbackContext connectionCallbackContext;
	private NetworkMonitor monitor = new NetworkMonitor();

    private String lastTypeOfNetwork;

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.connectionCallbackContext = null;

        this.registerConnectivityActionReceiver();
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("getConnectionInfo")) {
            this.connectionCallbackContext = callbackContext;
            String connectionType = this.getTypeOfNetworkFallbackToTypeNoneIfNotConnected();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, connectionType);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }
        return false;
    }

    /**
     * Stop network receiver.
     */
    public void onDestroy() {
        this.unregisterReceiver();
    }

    @Override
    public void onPause(boolean multitasking) {
        this.unregisterReceiver();
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        this.unregisterReceiver();
        this.registerConnectivityActionReceiver();
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    private void registerConnectivityActionReceiver() {
         monitor.addListener(() -> {
        	 updateConnectionInfo();
         });
         monitor.startMonitoring(3000); // check every 3 seconds
    }

    private void unregisterReceiver() {
    	monitor.stopMonitoring();
    }

    /**
     * Updates the JavaScript side whenever the connection changes
     *
     * @param info the current active network info
     * @return
     */
    private void updateConnectionInfo() {
        // send update to javascript "navigator.connection"
        // Jellybean sends its own info
        String currentNetworkType = this.getTypeOfNetworkFallbackToTypeNoneIfNotConnected();
        if (currentNetworkType.equals(this.lastTypeOfNetwork)) {
            LOG.d(LOG_TAG, "Networkinfo state didn't change, there is no event propagated to the JavaScript side.");
        } else {
            sendUpdate(currentNetworkType);
            this.lastTypeOfNetwork = currentNetworkType;
        }
    }

    /**
     * Gets the type of network connection of the NetworkInfo input
     *
     * @param info the current active network info
     * @return type the type of network
     */
    private String getTypeOfNetworkFallbackToTypeNoneIfNotConnected() {
    	try {
            InetAddress address = InetAddress.getByName("google.com");
            boolean reachable = address.isReachable(3000); // timeout 3 sec
            if (!reachable) {
            	return NetworkTypeDetector.NetworkType.unknown.toString();
            }
        } catch (Exception e) {
        	return NetworkTypeDetector.NetworkType.unknown.toString();
        }
        return NetworkTypeDetector.getPrimaryNetworkType().toString();
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param connection the network info to set as navigator.connection
     */
    private void sendUpdate(String type) {
        if (connectionCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, type);
            result.setKeepCallback(true);
            connectionCallbackContext.sendPluginResult(result);
        }
        webView.postMessage("networkconnection", type);
    }
}
