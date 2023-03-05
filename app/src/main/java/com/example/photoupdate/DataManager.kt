package com.example.photoupdate

import android.graphics.Bitmap

object DataManager {
    var barcode: String? = "Barcode is not scanned yet"
    var photo: Bitmap? = null

    var maxWidth: Int? = 1000
    var maxHeight: Int? = 1000
    var ftpHost: String? = "ftp.mikola.ro"
    var ftpUser: String? = "tester@mikola.ro"
    var ftpPassword: String? = "Tester_2023!"
}