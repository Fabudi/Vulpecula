package inc.fabudi.vulpecula.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import inc.fabudi.vulpecula.R
import inc.fabudi.vulpecula.databinding.FragmentMainBinding
import inc.fabudi.vulpecula.databinding.RouteRowBinding
import inc.fabudi.vulpecula.domain.Route
import inc.fabudi.vulpecula.viewmodels.RoutesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainFragment : Fragment() {

    private val viewModel: RoutesViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this, RoutesViewModel.Factory(activity.application)
        )[RoutesViewModel::class.java]
    }
    private lateinit var binding: FragmentMainBinding
    private var routesViewModelAdapter: RouteAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_main, container, false
        )
        binding.dateEditText.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
            datePicker.addOnPositiveButtonClickListener {
                binding.dateEditText.setText(
                    SimpleDateFormat(
                        "dd.MM.yyyy", Locale.getDefault()
                    ).format(Date(it)).toString()
                )
            }
            datePicker.show(childFragmentManager, "tag")
        }
        binding.searchButton.setOnClickListener {
            if (binding.fromEditText.text.toString() == "") {
                binding.fromEditText.error = "Empty input"
                return@setOnClickListener
            }
            if (binding.toEditText.text.toString() == "") {
                binding.toEditText.error = "Empty input"
                return@setOnClickListener
            }
            if (binding.dateEditText.text.toString() == "") {
                binding.dateEditText.error = "Empty input"
                return@setOnClickListener
            }
            viewModel.searchForDate(
                binding.fromEditText.text.toString(),
                binding.toEditText.text.toString(),
                SimpleDateFormat(
                    "yyyy-MM-dd", Locale.getDefault()
                ).format(
                    SimpleDateFormat(
                        "dd.MM.yyyy", Locale.getDefault()
                    ).parse(binding.dateEditText.text.toString())!!
                ).toString()
            )
        }
        routesViewModelAdapter = RouteAdapter(RouteClick {
            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            dialog.setContentView(view)
            dialog.show()
        })
        binding.ticketsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = routesViewModelAdapter
        }
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.stops.observe(viewLifecycleOwner) { stops ->
            stops.apply {
                val adapter = ArrayAdapter(
                    requireContext(), android.R.layout.simple_dropdown_item_1line, stops
                )
                binding.fromEditText.setAdapter(adapter)
                binding.toEditText.setAdapter(adapter)
                adapter.notifyDataSetChanged()
            }
        }
        viewModel.routes.observe(viewLifecycleOwner) { routes ->
            routes.apply {
                routesViewModelAdapter?.routes = routes
            }
        }
    }

}

class RouteAdapter(private val callback: RouteClick) : RecyclerView.Adapter<RouteViewHolder>() {
    var routes: List<Route> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val withDataBinding: RouteRowBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), RouteViewHolder.LAYOUT, parent, false
        )
        return RouteViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.viewDataBinding.also { binding ->
            binding.route = routes[position]
            binding.routeCallback = callback
        }
    }

    override fun getItemCount() = routes.size
}

class RouteViewHolder(val viewDataBinding: RouteRowBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.route_row
    }
}

class RouteClick(val block: (Route) -> Unit) {
    fun onClick(route: Route) = block(route)
}