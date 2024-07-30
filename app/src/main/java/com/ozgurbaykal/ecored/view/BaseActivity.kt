package com.ozgurbaykal.ecored.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ozgurbaykal.ecored.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

    private var progressBar: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressBar = findViewById(R.id.progressBar)
    }

    fun manageProgressBar(show: Boolean, timeout: Long? = null, onTimeout: (() -> Unit)? = null) {
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE

        if (!show) return

        timeout?.let {
            progressBar?.postDelayed({
                if(progressBar?.visibility == View.GONE)
                    return@postDelayed

                progressBar?.visibility = View.GONE
                onTimeout?.invoke()
            }, it)
        }
    }


    private fun changeFragment(fragment: Fragment, frameId: Int, tag: String) {
        supportFragmentManager.beginTransaction().apply {
            replace(frameId, fragment, tag)
            commit()
        }
    }

    fun changeFragmentWithBundle(fragment: Fragment, frameId: Int, tag: String, bundle: Bundle? = null) {
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(frameId, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }
}