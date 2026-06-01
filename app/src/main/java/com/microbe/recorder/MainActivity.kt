package com.microbe.recorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.microbe.recorder.fragment.ExportFragment
import com.microbe.recorder.fragment.HistoryFragment
import com.microbe.recorder.fragment.ManageFragment
import com.microbe.recorder.fragment.NewRecordFragment

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // 默认显示新建记录
        if (savedInstanceState == null) {
            loadFragment(NewRecordFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_new_record -> { loadFragment(NewRecordFragment()); true }
                R.id.nav_export -> { loadFragment(ExportFragment()); true }
                R.id.nav_history -> { loadFragment(HistoryFragment()); true }
                R.id.nav_manage -> { loadFragment(ManageFragment()); true }
                else -> false
            }
        }

        requestPermissions()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, toRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
}
