package inc.fabudi.vulpecula.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.fabudi.vulpecula.repository.RoutesFirebaseRepository

class RoutesViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = RoutesFirebaseRepository()
    var stops = repository.stops
    var routes = repository.routes

    fun searchForDate(from: String, to: String, date: String) {
        Log.d("Search for:", "$from, $to, $date")
        repository.searchForRoutes(from, to, date)
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoutesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return RoutesViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}