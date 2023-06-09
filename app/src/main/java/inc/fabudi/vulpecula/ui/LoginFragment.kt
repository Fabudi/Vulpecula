package inc.fabudi.vulpecula.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import inc.fabudi.vulpecula.R
import inc.fabudi.vulpecula.databinding.FragmentLoginBinding
import inc.fabudi.vulpecula.viewmodels.LoginViewModel


class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this, LoginViewModel.Factory(activity.application)
        )[LoginViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (viewModel.isLoggedIn()) {
            (activity as MainActivity).navController.navigate(R.id.action_loginFragment_to_mainFragment)
        }
        val binding: FragmentLoginBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_login, container, false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.sendPhoneButton.setOnClickListener {
            viewModel.login(binding.phoneEditText.text.toString())
        }
        binding.sendCodeButton.setOnClickListener {
            viewModel.completeLogin(binding.codeEditText.text.toString())
        }
        binding.infoButton.setOnClickListener {
            viewModel.writeUserToDatabase(
                binding.nameEditText.text.toString(), binding.lastnameEditText.text.toString()
            )
            (activity as MainActivity).navController.navigate(R.id.action_loginFragment_to_mainFragment)
        }
        return binding.root
    }

}