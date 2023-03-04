package com.example.photoupdate

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity() : AppCompatActivity() {
    var sdkVersion = Build.VERSION.SDK_INT
    var model = Build.MODEL
    private val updateConversationHandler: Handler? = null

    private val REQUEST_CODE = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView.setText("Barcode is not scanned yet")

        btnPhoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if(takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        //Register receiver so my app can listen for intents which action is ACTION_BARCODE_DATA
        val intentFilter = IntentFilter(ACTION_BARCODE_DATA)
        registerReceiver(barcodeDataReceiver, intentFilter)

        //Will setup the new configuration of the scanner.
        claimScanner()
    }

    // CAMERA PHOTO
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val takenImage = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(takenImage)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private val barcodeDataReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Toast.makeText(context, "Received the Broadcast Intent", Toast.LENGTH_SHORT).show()
            val action = intent.action
            println("Action Received: $action")
            if (ACTION_BARCODE_DATA == action) {
                /*
                These extras are available:
                    "version" (int) = Data Intent Api version
                    "aimId" (String) = The AIM Identifier
                    "charset" (String) = The charset used to convert "dataBytes" to "data" string
                    "codeId" (String) = The Honeywell Symbology Identifier
                    "data" (String) = The barcode data as a String
                    "dataBytes" (byte[]) = The barcode data as a byte array
                    "timestamp" (String) = The barcode timestamp
                 */
                val version = intent.getIntExtra("version", 0)
                if (version >= 1) {
                    val aimId = intent.getStringExtra("aimId")
                    val charset = intent.getStringExtra("charset")
                    val codeId = intent.getStringExtra("codeId")
                    val data = intent.getStringExtra("data")
                    val dataBytes = intent.getByteArrayExtra("dataBytes")
                    val dataBytesStr = bytesToHexString(dataBytes)
                    val timestamp = intent.getStringExtra("timestamp")
                    val text = "Barcode: $data"
                    Log.e(TAG, "Received the scanned barcode")
                    setText(text)
                }
            }
        }
    }

    private fun claimScanner() {
        val properties = Bundle()


        //When we press the scan button and read a barcode, a new Broadcast intent will be launched by the service
        properties.putBoolean("DPR_DATA_INTENT", true)

        //That intent will have the action "ACTION_BARCODE_DATA"
        // We will capture the intents with that action (every scan event while in the application)
        // in our BroadcastReceiver barcodeDataReceiver.
        properties.putString("DPR_DATA_INTENT_ACTION", ACTION_BARCODE_DATA)
        //properties.putString("TRIGGER_MODE", "continuous");
        val intent = Intent()
        intent.action = ACTION_CLAIM_SCANNER

        /*
         * We use setPackage() in order to send an Explicit Broadcast Intent, since it is a requirement
         * after API Level 26+ (Android 8)
         */intent.setPackage("com.intermec.datacollectionservice")

        //We will use the internal scanner
        intent.putExtra(EXTRA_SCANNER, "dcs.scanner.imager")

        /*
        We are using "MyProfile1", so a profile with this name has to be created in Scanner settings:
               Android Settings > Honeywell Settings > Scanning > Internal scanner > "+"
        - If we use "DEFAULT" it will apply the settings from the Default profile in Scanner settings
        - If not found, it will use Factory default settings.
         */intent.putExtra(EXTRA_PROFILE, "MyProfile1")
        intent.putExtra(EXTRA_PROPERTIES, properties)
        sendBroadcast(intent)
        Toast.makeText(this, "Scanner Claimed", Toast.LENGTH_SHORT).show()
    }

    fun triggerScanner(view: View?) {
        sendBroadcast(Intent(EXTRA_CONTROL)
            .setPackage("com.intermec.datacollectionservice")
            .putExtra(EXTRA_SCAN, true)
        )
        Toast.makeText(this, "Releasing the Scanner", Toast.LENGTH_SHORT).show()
    }

    private fun setText(text: String) {
        if (textView != null) {
            runOnUiThread { textView!!.text = text }
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(barcodeDataReceiver)
        releaseScanner()
    }

    private fun releaseScanner() {
        val intent = Intent()
        intent.action = ACTION_RELEASE_SCANNER
        sendBroadcast(intent)
    }

    private fun bytesToHexString(array: ByteArray?): String {
        var s = "[]"
        if (array != null) {
            s = "["
            for (i in array.indices) {
                s += "0x" + Integer.toHexString(array[i].toInt()) + ", "
            }
            s = s.substring(0, s.length - 2) + "]"
        }
        return s
    }

    companion object {
        private val TAG = "IntentApiSample"
        private val EXTRA_CONTROL = "com.honeywell.aidc.action.ACTION_CONTROL_SCANNER"
        private val EXTRA_SCAN = "com.honeywell.aidc.extra.EXTRA_SCAN"
        val ACTION_BARCODE_DATA = "com.honeywell.sample.intentapisample.BARCODE"

        /**
         * Honeywell DataCollection Intent API
         * Claim scanner
         * Permissions:
         * "com.honeywell.decode.permission.DECODE"
         */
        val ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER"

        /**
         * Honeywell DataCollection Intent API
         * Release scanner claim
         * Permissions:
         * "com.honeywell.decode.permission.DECODE"
         */
        val ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER"

        /**
         * Honeywell DataCollection Intent API
         * Optional. Sets the scanner to claim. If scanner is not available or if extra is not used,
         * DataCollection will choose an available scanner.
         * Values : String
         * "dcs.scanner.imager" : Uses the internal scanner
         * "dcs.scanner.ring" : Uses the external ring scanner
         */
        val EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER"

        /**
         * Honeywell DataCollection Instent API
         * Optional. Sets the profile to use. If profile is not available or if extra is not used,
         * the scanner will use factory default properties (not "DEFAULT" profile properties).
         * Values : String
         */
        val EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE"

        /**
         * Honeywell DataCollection Intent API
         * Optional. Overrides the profile properties (non-persistend) until the next scanner claim.
         * Values : Bundle
         */
        val EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES"
    }
}


