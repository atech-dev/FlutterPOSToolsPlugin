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
  private val REQUEST_CODE = 0
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var activity: Activity
  private lateinit var permissionHandler: PermissionHandler

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_pos_tools")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if (call.method == "getSerialNumber") {
        handleGetSerialNumber(call, result)
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

    override fun onAttachedToActivity(@NonNull activityPluginBinding: ActivityPluginBinding) {
        activity = activityPluginBinding.getActivity();

        permissionHandler = PermissionHandler();
        activityPluginBinding.addRequestPermissionsResultListener(this);
    }

    override fun onReattachedToActivityForConfigChanges(@NonNull activityPluginBinding: ActivityPluginBinding) {
        onAttachedToActivity(activityPluginBinding);
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onDetachedFromActivity() {
        channel.setMethodCallHandler(null)
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
                    activity.requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_CODE);
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

                    if(permissionHandler != null) {
                        permissionHandler.setPermissionStatus(true)
                    }

                } else {
                    // not granted
                    if(permissionHandler != null) {
                        permissionHandler.setPermissionStatus(false)
                    }
                }

                return true
            }
            else -> permissionHandler.setPermissionStatus(false)
                // super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        return true
    }

}

class PermissionHandler {
    private var listener: PermissionListener? = null

    // Constructor where listener events are ignored
    init {
        // set null or default listener or accept as argument to constructor
        this.listener = null
    }

    interface PermissionListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        fun onPermissionResult(status: Boolean)

    // or when data has been loaded
    // fun onDataLoaded(data: SomeData?)
    }

    // Assign the listener implementing events interface that will receive the events
    fun setPermissionListener(listener: PermissionListener) {
        this.listener = listener
    }

    fun setPermissionStatus(status: Boolean) {
        this.listener?.onPermissionResult(status)
    }
}
