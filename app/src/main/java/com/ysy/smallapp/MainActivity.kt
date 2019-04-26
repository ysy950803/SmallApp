package com.ysy.smallapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtText = findViewById<EditText>(R.id.edt_main)

        findViewById<View>(R.id.btn_main).setOnClickListener {
            startActivity(Intent().apply {
                val id = edtText.text.toString().toInt() % 5
                setClassName(this@MainActivity, "com.ysy.smallapp.SmallActivity\$Small$id")
            })
        }
    }
}
