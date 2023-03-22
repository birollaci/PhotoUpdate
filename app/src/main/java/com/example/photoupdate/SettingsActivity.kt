package com.example.photoupdate

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.photoupdate.DataManager.ftpHost
import com.example.photoupdate.DataManager.ftpPassword
import com.example.photoupdate.DataManager.ftpUser
import com.example.photoupdate.DataManager.maxHeight
import com.example.photoupdate.DataManager.maxWidth
import kotlinx.android.synthetic.main.activity_settings.*
import java.text.DecimalFormat
import java.text.NumberFormat

class SettingsActivity() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val formatter: NumberFormat = DecimalFormat("#0")
        maxWidthEditText.setText(formatter.format(maxWidth), TextView.BufferType.EDITABLE)
        maxHeightEditText.setText(formatter.format(maxHeight), TextView.BufferType.EDITABLE)
        ftpHostEditText.setText(ftpHost, TextView.BufferType.EDITABLE)
        ftpUserEditText.setText(ftpUser, TextView.BufferType.EDITABLE)
        ftpPasswordEditText.setText(ftpPassword, TextView.BufferType.EDITABLE)

        btnUpdate.setOnClickListener {
            val password = passwordEditText.text.toString()
            if(password == "Auto.ID") {
                if(maxWidthEditText.text.toString().isEmpty()
                    || maxHeightEditText.text.toString().isEmpty()
                    || ftpHostEditText.text.toString().isEmpty()
                    || ftpUserEditText.text.toString().isEmpty()
                    || ftpPasswordEditText.text.toString().isEmpty()) {
                    alertMsg("Please complete the missing field(s)!")
                } else {
                    maxWidth = maxWidthEditText.text.toString().toInt()
                    maxHeight = maxHeightEditText.text.toString().toInt()
                    ftpHost = ftpHostEditText.text.toString()
                    ftpUser = ftpUserEditText.text.toString()
                    ftpPassword = ftpPasswordEditText.text.toString()
                    alertMsg("Update finished!")
                    passwordEditText.setText("", TextView.BufferType.EDITABLE)
                }
            } else {
                alertMsg("Wrong password")
            }
        }

    }

    private fun alertMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}


