package com.hueyra.biometricauth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.hueyra.biometricauth.BiometricAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BiometricAuth.getInstance().init(this)

        findViewById<View>(R.id.touch_id).setOnClickListener {
            startActivity(Intent(this, AuthTouchIDActivity::class.java))
        }
        findViewById<View>(R.id.face_id).setOnClickListener {
            startActivity(Intent(this, AuthFaceIDActivity::class.java))
        }

    }
}