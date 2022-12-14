package com.nandaiqbalh.themovielisting.presentation.ui.user.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nandaiqbalh.themovielisting.R
import com.nandaiqbalh.themovielisting.databinding.FragmentLoginBinding
import com.nandaiqbalh.themovielisting.di.UserServiceLocator
import com.nandaiqbalh.themovielisting.presentation.ui.movie.HomeActivity
import com.nandaiqbalh.themovielisting.util.viewModelFactory


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModelFactory {
        LoginViewModel(UserServiceLocator.provideUserRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener { checkLogin() }

        if (isUserLoggedIn()) {
            navigateToHome()
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

    }

    private fun checkLogin() {
        if (validateInput()) {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            viewModel.getIfUserExist(username)
            viewModel.getIfUserExistResult.observe(viewLifecycleOwner) { exist ->
                if (exist) {
                    viewModel.checkIsUserLoginValid(username, password)
                    viewModel.checkIsUserLoginValid.observe(viewLifecycleOwner) {
                        setSharedPreference(username)
                        checkUser(it)
                    }
                } else {
                    setLoginState("Username not found")
                }
            }
        }
    }

    private fun setSharedPreference(username: String) {
        viewModel.getUserByUsername(username)
        viewModel.userByUsernameResult.observe(viewLifecycleOwner) {
            viewModel.setUserId(it.userId)
        }
    }

    private fun checkUser(userLoggedIn: Boolean?) {

        if (validateInput()) {
            userLoggedIn?.let {
                if (userLoggedIn) {
                    navigateToHome()
                    setLoginState("Login Success")
                } else {
                    setLoginState("Wrong password")
                }
                viewModel.setIfUserLogin(userLoggedIn)
            }
        }
    }

    private fun setLoginState(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun isUserLoggedIn(): Boolean {
        return viewModel.checkIfUserLoggedIn()
    }
    private fun validateInput(): Boolean {
        var isValid = true
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        if (username.isEmpty()) {
            isValid = false
            binding.etUsername.error = "Username must not be empty"
        }
        if (password.isEmpty()) {
            isValid = false
            Toast.makeText(requireContext(), "Password must not be empty", Toast.LENGTH_SHORT)
                .show()
        }
        return isValid
    }

    private fun navigateToHome() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}