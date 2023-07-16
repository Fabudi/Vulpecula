package inc.fabudi.vulpecula.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import inc.fabudi.vulpecula.domain.Route
import inc.fabudi.vulpecula.domain.Stop

class RoutesFirebaseRepository {
    private val databaseReference = Firebase.database.reference

    var stops = MutableLiveData<List<Stop>>()

    var routes = MutableLiveData<List<Route>>()

    init {
        refreshStops()
    }

    private fun refreshStops() {
        databaseReference.child("stops").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stopsArray = mutableListOf<Stop>()
                for (stop in snapshot.children) {
                    stopsArray.add(stop.getValue<Stop>()!!)
                }
                stops.postValue(stopsArray)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Stops", "refreshStops:onCancelled", error.toException())
            }

        })
    }

    fun searchForRoutes(from: String, to: String, date: String) {
        databaseReference.child("routes").orderByChild("departureDate").equalTo(date)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Routes", "Smt happened")
                    val routesArray = mutableListOf<Route>()
                    for (item in snapshot.children) {
                        val route = item.getValue<Route>()!!
                        Log.d("Routes", route.toString())
                        if (route.from == from && route.to == to) routesArray.add(route)
                    }
                    routes.postValue(routesArray)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Routes", "refreshStops:onCancelled", error.toException())
                }

            })
    }

}