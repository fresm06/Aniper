package com.aniper.ui.creator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aniper.R
import com.google.android.material.button.MaterialButton

class CreatorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_creator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btn_preview).setOnClickListener {
            Toast.makeText(requireContext(), "Preview: requires Firebase (coming soon)", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialButton>(R.id.btn_upload).setOnClickListener {
            Toast.makeText(requireContext(), "Upload: requires Firebase (coming soon)", Toast.LENGTH_SHORT).show()
        }
    }
}
