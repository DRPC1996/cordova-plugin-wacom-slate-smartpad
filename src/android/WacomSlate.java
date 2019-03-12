package cordova.plugin.wacomslate;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.wacom.bootstrap.lib.InvalidLicenseException;
import com.wacom.cdl.callbacks.AlertsCallback;
import com.wacom.cdl.enums.InkDeviceAlert;
import com.wacom.cdl.enums.InkDeviceEvent;
import com.wacom.cdl.InkDeviceInfo;
import com.wacom.cdl.enums.InkDeviceStatus;
import com.wacom.cdl.enums.UserAction;
import com.wacom.cdl.callbacks.EventCallback;
import com.wacom.cdl.InkDevice;
import com.wacom.cdl.InkDeviceFactory;
import com.wacom.cdl.deviceservices.EventDeviceService;
import com.wacom.cdl.deviceservices.DeviceServiceType;
import com.wacom.cdl.SmartPadDevice;

import java.nio.charset.Charset;
import java.lang.Object;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class WacomSlate extends CordovaPlugin {

    @Override
    public void initialize (CordovaInterface cordova, CordovaWebView webView)
    {
        this.web = webView;
        Context context = cordova.getActivity().getApplicationContext();

        SmartPadDevice smartPadDevice = new SmartPadDevice();
        smartPadDevice.SmartPadDevice(Context context, InkDeviceInfo inkDeviceInfo);
        smartPadDevice.connect(InkDeviceInfo inkDeviceInfo, byte[] appId, ConnectionCallback callback);

        java.util.ArrayList<DeviceServiceType> = smartPadDevice.getAvailableDeviceServices();

        InkDeviceService inkDeviceService = smartPadDevice.getDeviceService(DeviceServiceType deviceServiceType);
        InkDeviceStatus inkDeviceStatus = smartPadDevice.getStatus();

        smartPadDevice.getProperties(java.util.List<InkDeviceProperty> properties, GetPropertiesCallback callback);
        smartPadDevice.setProperty(InkDeviceProperty property, java.lang.Object value, SetPropertyCallback callback);
        smartPadDevice.subscribe(AlertsCallback alertsCallback);
        smartPadDevice.dispose();     
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {
            String message = args.getString(0);
            callbackContext.success(message);
            System.out.println("start method called")
            return true;
        }
        return false;
    }

}
