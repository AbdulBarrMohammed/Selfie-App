package com.example.selfieassignment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.selfieassignment.databinding.FragmentSignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignUpFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //auth = FirebaseAuth.getInstance()
        auth = Firebase.auth

        binding.registerBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passEt.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                findNavController().navigate(R.id.action_signUpFragment_to_signInFragment2)
                            } else {
                                Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()

                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}