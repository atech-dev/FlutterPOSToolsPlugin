package ao.co.atech.dev.flutter_pos_tools

import android.Manifest
import android.telephony.TelephonyManager
import androidx.annotation.NonNull

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.reflect.Method

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

import io.flutter.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import io.flutter.plugin.common.PluginRegistry

/** FlutterPosToolsPlugin */
class FlutterPosToolsPlugin: FlutterPlugin, ActivityAware, MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {

  private val TAG = "FlutterPosToolsPlugin"
  private val REQUEST_CODE = 100
  
  private lateinit var channel : MethodChannel
  private lateinit var activity: Activity
  private lateinit var permissionHandler: PermissionHandler
  private lateinit var activityBinding: ActivityPluginBinding;

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_pos_tools")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(@NonNull activityPluginBinding: ActivityPluginBinding) {
        activityBinding = activityPluginBinding;
        activity = activityBinding.getActivity();

        permissionHandler = PermissionHandler();
        activityBinding.addRequestPermissionsResultListener(this);
    }

    override fun onReattachedToActivityForConfigChanges(@NonNull activityPluginBinding: ActivityPluginBinding) {
        activityBinding = activityPluginBinding;
        activity = activityBinding.getActivity();

        permissionHandler = PermissionHandler();
        activityBinding.addRequestPermissionsResultListener(this);
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activityBinding.removeRequestPermissionsResultListener(this);
        activityBinding = null;
        activity = null;
    }

    override fun onDetachedFromActivity() {
        activityBinding.removeRequestPermissionsResultListener(this);
        activityBinding = null;
        activity = null;
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "getSerialNumber") {
            if (activity == null) {
                Log.e(TAG, "getSerialNumber, activity is null");
                result.error(null, "getSerialNumber, activity is null", null);
                return;
            }
            handleGetSerialNumber(call, result)
        } else {
            result.notImplemented()
        }
    }

    private fun handleGetSerialNumber(call: MethodCall, result: Result) {
        try {
            when {
                hasPermission() -> getSerialNumber(result)
                else -> {
                    permissionHandler.setPermissionListener(object : PermissionHandler.PermissionListener {
                        override fun onPermissionResult(status: Boolean) {
                            if(status) {
                                getSerialNumber(result)
                            } else {
                                result.error("UnknownError", "Device doesn't has a permission to get serial", null)
                            }

                        }
                    })
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        activity.requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_CODE)
                    }
                }
            }
        } catch(e: Exception) {

            e.message?.let {
                Log.e(TAG, it)
                result.error(
                    "$TAG Exception",
                    "Unkown error $it",
                    null
                )
            }

        }
    }

    private fun getSerialNumber(result: Result) {
        val telephonyManager = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var serialNumber: String?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "Build.getSerial(): ${Build.getSerial()}")
            serialNumber = Build.getSerial()
        } else {
            Log.i(TAG, "Build.Serial: ${Build.SERIAL}")
            serialNumber = Build.SERIAL
        }

        if(serialNumber == null || serialNumber.equals("")) {
            val c = Class.forName("android.os.SystemProperties")
            val get: Method = c.getMethod("get", String::class.java)

            serialNumber = ""

            if (serialNumber.equals("")) serialNumber =
                get.invoke(c, "ril.serialnumber") as String
            if (serialNumber.equals("")) serialNumber =
                get.invoke(c, "ro.serialno") as String
            if (serialNumber.equals("")) serialNumber =
                get.invoke(c, "sys.serialnumber") as String
            if (serialNumber.equals("")) serialNumber = Build.SERIAL

            // If none of the methods above worked

            Log.i(TAG, "SERIAL NUM: $serialNumber")
        }

        if(serialNumber == null) {
            result.error("SERIAL NUMBER ERROR", "Impossible to get serial number", null)
        } else {
            result.success(serialNumber)
        }
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        when (requestCode) {
            REQUEST_CODE -> {
                if (hasPermission()) {

                    permissionHandler.setPermissionStatus(true)

                } else {
                    // not granted
                    permissionHandler.setPermissionStatus(false)
                }
            }
            // else -> permissionHandler.setPermissionStatus(false)
        }
        return true
    }

}

class PermissionHandler {
    private var listener: PermissionListener? = null

    init {
        this.listener = null
    }

    interface PermissionListener {
        fun onPermissionResult(status: Boolean)
    }

    fun setPermissionListener(listener: PermissionListener) {
        this.listener = listener
    }

    fun setPermissionStatus(status: Boolean) {
        this.listener?.onPermissionResult(status)
    }
}
