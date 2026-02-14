package com.aniper.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aniper.R
import com.google.android.material.progressindicator.CircularProgressIndicator

class AdminFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<CircularProgressIndicator>(R.id.progress_bar).visibility = View.GONE
        view.findViewById<View>(R.id.layout_access_denied).visibility = View.VISIBLE
        view.findViewById<TextView>(R.id.tv_admin_empty).visibility = View.GONE
    }
}
