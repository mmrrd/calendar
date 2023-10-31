package com.example.calendarapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.calendarapp.R
import java.io.IOException



class PrivacyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowTitleEnabled(false)
        //actionBar.setTitle("Privacy");
        val textView = findViewById<TextView>(R.id.privacytext)
        textView.text = LoadData("licence.txt")
    }

    fun LoadData(inFile: String?): String {
        var tContents = ""
        try {
            val stream = assets.open(inFile!!)
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            tContents = buffer.toString()
        } catch (e: IOException) {
            // Handle exceptions here
        }
        return tContents
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}