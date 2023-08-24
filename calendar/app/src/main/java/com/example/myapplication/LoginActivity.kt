package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.ui.login.LoginFragment

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(localClassName, "onCreate")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        supportFragmentManager.beginTransaction()
            .replace(R.id.login_fragment, LoginFragment())
            .commit()
    }
}
