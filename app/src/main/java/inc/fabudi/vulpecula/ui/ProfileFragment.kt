package inc.fabudi.vulpecula.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import inc.fabudi.vulpecula.R
import inc.fabudi.vulpecula.databinding.FragmentProfileBinding
import inc.fabudi.vulpecula.viewmodels.ProfileViewModel


class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this, ProfileViewModel.Factory(activity.application)
        )[ProfileViewModel::class.java]
    }

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile, container, false
        )
        binding.viewModel = viewModel
        binding.profileAppBarBackButton.setOnClickListener {
            (activity as MainActivity).navController.navigate(R.id.action_profileFragment_to_mainFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}