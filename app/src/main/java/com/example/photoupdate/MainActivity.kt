package com.example.photoupdate

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.photoupdate.DataManager.photo
import com.example.photoupdate.DataManager.barcode
import com.example.photoupdate.DataManager.ftpHost
import com.example.photoupdate.DataManager.ftpPassword
import com.example.photoupdate.DataManager.ftpUser
import com.example.photoupdate.DataManager.maxHeight
import com.example.photoupdate.DataManager.maxWidth
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity() : AppCompatActivity() {

    private val REQUEST_CODE = 42

    private var imgWidth: Int = 0
    private var imgHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView.setText("Barcode: $barcode")

        if(photo != null) {
            imageView.setImageBitmap(photo)
        }

        btnPhoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if(takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }

        }

        btnSendFtp.setOnClickListener {
            if(barcode == "Barcode is not scanned yet") {
                alertMsg("Please scan a barcode or QR code!")
            } else if(photo == null) {
                alertMsg("Please make a photo!")
            } else {
                if(widthEditText.text.toString().isEmpty() || heightEditText.text.toString().isEmpty()) {
                    alertMsg("Please fill width and height of image!")
                } else {
                    imgWidth = widthEditText.text.toString().toInt()
                    imgHeight = heightEditText.text.toString().toInt()

                    if (imgWidth == 0 || imgHeight == 0) {
                        alertMsg("Width and height be greater than 0 !")
                    } else {
                        if(imgWidth > maxWidth!! || imgHeight > maxHeight!!) {
                            alertMsg("Width or height is exceeding max values! maxWidth=$maxWidth, maxHeight=$maxHeight")
                        } else {
                            sendFTP()
                        }
                    }
                }
            }
        }

        var eighth = 8

        logoImageView.setOnClickListener {
            if(eighth == 1) {
                eighth = 8
                startActivity(Intent(this, SettingsActivity::class.java))
            }else{
                eighth--
            }

        }
    }

    // SEND to FTP server
    private fun sendFTP() {
        Thread(Runnable {
            try {
                try {
                    val ftp = FTPClient()
                    ftp.connect(ftpHost)
                    if(ftp.login(ftpUser, ftpPassword)){
                        ftp.type(FTPClient.BINARY_FILE_TYPE)

                        // Elso mappa
                        ftp.changeToParentDirectory()
                        val simpleDateFormat = SimpleDateFormat("yyyyMMdd")
                        val mainDir = simpleDateFormat.format(Date())
                        // ezzel megnezzuk, hogy letezik-e a mappa
                        ftp.changeWorkingDirectory(mainDir);
                        var returnCode = ftp.replyCode;
                        // Ha letezik
                        if (returnCode == 550) {
                            ftp.changeToParentDirectory()
                            ftp.makeDirectory(mainDir)
                            ftp.changeWorkingDirectory(mainDir)
                        } // kulonben semmi

                        // Masodik mappa
                        val secondDir = barcode
                        ftp.changeWorkingDirectory(secondDir);
                        returnCode = ftp.replyCode;
                        if (returnCode == 550) {
                            ftp.changeWorkingDirectory(mainDir);
                            ftp.makeDirectory(secondDir)
                        }

                        // Kep feltoltese
                        val simpleDateFormat2 = SimpleDateFormat("yyyyMMddhhmmss")
                        val imgName = simpleDateFormat2.format(Date())
                        ftp.changeWorkingDirectory(secondDir)
                        ftp.enterLocalPassiveMode()
                        ftp.setFileType(FTP.BINARY_FILE_TYPE)

                        val resizedPhoto = getResizedBitmap(photo!!, imgWidth, imgHeight)

                        val bos = ByteArrayOutputStream()
                        resizedPhoto!!.compress(CompressFormat.PNG, 0, bos)
                        val bs : InputStream = ByteArrayInputStream(bos.toByteArray())

                        ftp.storeFile("$imgName.jpg", bs)

                        ftp.logout()
                    }
                    ftp.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun alertMsg(msg: String) {
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
    }

    // CAMERA PHOTO
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val takenImage = data?.extras?.get("data") as Bitmap
            photo = takenImage
            imageView.setImageBitmap(photo)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false)
        bm.recycle()
        return resizedBitmap
    }

    private fun saveImage(bitmap: Bitmap) {
        val file = getDisc()

        if (!file.exists() && !file.mkdirs()) {
            file.mkdir()
        }

        val simpleDateFormat = SimpleDateFormat("yyyymmsshhmmss")
        val date = simpleDateFormat.format(Date())
        val name = "sajatkep.jpg"
        val fileName = file.absolutePath + "/" + name
        val newFile = File(fileName)

        try {
            val fileOutPutStream = FileOutputStream(newFile)
            bitmap.compress(CompressFormat.JPEG, 100, fileOutPutStream)
            fileOutPutStream.flush()
            fileOutPutStream.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getDisc(): File {
        val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(file, "PhotoUpdate")
    }

    override fun onResume() {
        super.onResume()
        //Register receiver so my app can listen for intents which action is ACTION_BARCODE_DATA
        val intentFilter = IntentFilter(ACTION_BARCODE_DATA)
        registerReceiver(barcodeDataReceiver, intentFilter)

        //Will setup the new configuration of the scanner.
        claimScanner()
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
                    barcode = data
                    val text = "Barcode: $barcode"
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


