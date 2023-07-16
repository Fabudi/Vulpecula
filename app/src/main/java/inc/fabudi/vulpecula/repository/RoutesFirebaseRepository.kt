package inc.fabudi.vulpecula.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import inc.fabudi.vulpecula.domain.Route
import inc.fabudi.vulpecula.domain.Stop
import inc.fabudi.vulpecula.domain.Ticket

class RoutesFirebaseRepository {
    private val databaseReference = Firebase.database.reference

    var stops = MutableLiveData<List<Stop>>()

    var routes = MutableLiveData<List<Route>>()

    var successfulOrder = MutableLiveData(false)
    var orderInProcess = MutableLiveData(false)
    var orderCompleted = MutableLiveData(false)

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
                    val routesArray = mutableListOf<Route>()
                    for (item in snapshot.children) {
                        val route = item.getValue<Route>()!!
                        if (route.from == from && route.to == to && route.ticketsLeft.toString()
                                .toInt() > 0
                        ) routesArray.add(route)
                    }
                    routes.postValue(routesArray)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Routes", "refreshStops:onCancelled", error.toException())
                }

            })
    }

    fun makeOrder(route: Route, selectedQty: Int) {
        orderInProcess.postValue(true)
        val ticketKey = databaseReference.child("tickets").push().key
        databaseReference.child("buses").child(route.bus.toString()).child("color").get()
            .addOnSuccessListener { bus ->
                databaseReference.child("users").child(Firebase.auth.currentUser?.uid.toString())
                    .child("tickets").get().addOnSuccessListener {
                        val color = bus.getValue<String>()
                        var tickets = it.getValue<List<String>>()
                        tickets = if (tickets == null) mutableListOf()
                        else tickets as MutableList<String>
                        val ticket = Ticket(
                            ticketKey.toString(), color.toString(), selectedQty.toString(), route
                        )
                        tickets.add(ticket.id.toString())
                        val childUpdates = hashMapOf(
                            "/routes/${route.id}/ticketsLeft" to (route.ticketsLeft.toString()
                                .toInt() - selectedQty).toString(),
                            "/users/${Firebase.auth.currentUser?.uid}/tickets" to tickets,
                            "/tickets/$ticketKey" to ticket
                        )
                        try {
                            databaseReference.updateChildren(childUpdates)
                            successfulOrder.postValue(true)
                        } catch (e: Exception) {
                            successfulOrder.postValue(false)
                            Log.e("Order", e.toString())
                        }finally {
                            orderInProcess.postValue(false)
                            orderCompleted.postValue(true)
                        }
                    }
            }
    }

}