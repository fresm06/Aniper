package com.aniper.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aniper.R
import com.google.android.material.button.MaterialButton

class AuthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btn_google_sign_in).setOnClickListener {
            // Local test mode: skip actual sign-in, go straight to home
            Toast.makeText(requireContext(), "Test mode: skipping sign in", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_auth_to_home)
        }
    }
}
