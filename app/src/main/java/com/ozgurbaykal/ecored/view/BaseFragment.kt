package com.ozgurbaykal.ecored.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.ozgurbaykal.ecored.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseFragment : Fragment() {

    private var progressBar: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.progressBar)
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


     fun changeFragment(fragment: Fragment, frameId: Int, tag: String) {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(frameId, fragment, tag)
            addToBackStack(tag)
            commit()
        }
    }

    fun changeFragmentWithBundle(fragment: Fragment, frameId: Int, tag: String, bundle: Bundle? = null) {
        fragment.arguments = bundle
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(frameId, fragment, tag)
            addToBackStack(tag)
            commit()
        }
    }

}