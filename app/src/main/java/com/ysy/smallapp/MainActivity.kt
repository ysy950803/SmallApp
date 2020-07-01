package com.ysy.smallapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_main.setOnClickListener {
            startActivity(Intent().apply {
                val id = edt_main.text.toString().toInt() % 5
                setClassName(this@MainActivity, "com.ysy.smallapp.SmallActivity\$Small$id")
            })
        }
    }

    // 另一种方式，仅供参考
    fun startSmallAppAtNewTask() {
        startActivity(Intent().apply {
            setClassName(this@MainActivity, "com.xxx.xxx.XXXActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT and Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        })
    }
}
