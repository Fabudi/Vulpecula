package inc.fabudi.vulpecula.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import inc.fabudi.vulpecula.domain.Route
import inc.fabudi.vulpecula.domain.Ticket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TicketsFirebaseRepository {
    private val databaseReference = Firebase.database.reference
    var noTickets = MutableLiveData(false)
    var noActiveTickets = ObservableBoolean(false)
    var noBookedTickets = ObservableBoolean(false)
    var noArchivedTickets = ObservableBoolean(false)
    var activeTickets = MutableLiveData<List<Ticket>>()
    var bookedTickets = MutableLiveData<List<Ticket>>()
    var archivedTickets = MutableLiveData<List<Ticket>>()

    init {
        loadTickets()
    }

    private fun loadTickets() {
        databaseReference.child("users").child(Firebase.auth.uid.toString()).child("tickets")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ticketsIds = mutableListOf<String>()
                    for (stop in snapshot.children) {
                        ticketsIds.add(stop.getValue<String>()!!)
                    }
                    databaseReference.child("tickets")
                        .addValueEventListener(object : ValueEventListener {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val tickets = mutableListOf<Ticket>()
                                for (ticket in snapshot.children) {
                                    tickets.add(ticket.getValue<Ticket>()!!)
                                }
                                archivedTickets.postValue(tickets.filter { ticket ->
                                    LocalDateTime.parse(
                                        ticket.arrivalDate + ticket.arrivalTime,
                                        DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm")
                                    ) < LocalDateTime.now()
                                }.apply {
                                    noArchivedTickets.set(this.isEmpty())
                                })
                                bookedTickets.postValue(tickets.filter { ticket ->
                                    LocalDateTime.parse(
                                        ticket.arrivalDate + ticket.arrivalTime,
                                        DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm")
                                    ) > LocalDateTime.now() && LocalDateTime.parse(
                                        ticket.departureDate + ticket.departureTime,
                                        DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm")
                                    ) > LocalDateTime.now()
                                }.apply {
                                    noBookedTickets.set(this.isEmpty())
                                })
                                activeTickets.postValue(tickets.filter { ticket ->
                                    LocalDateTime.parse(
                                        ticket.departureDate + ticket.departureTime,
                                        DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm")
                                    ) < LocalDateTime.now() && LocalDateTime.parse(
                                        ticket.arrivalDate + ticket.arrivalTime,
                                        DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm")
                                    ) > LocalDateTime.now()
                                }.apply {
                                    noActiveTickets.set(this.isEmpty())
                                    Log.d("Active", this.isEmpty().toString())
                                    Log.d("Active", noActiveTickets.get().toString())
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("Sorted Tickets", error.toString())
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("User Tickets", error.toString())
                    noTickets.postValue(true)
                }

            })
    }

    fun cancelOrder(ticket: Ticket) {
        databaseReference.child("tickets").get().addOnSuccessListener {
            val ticketsArray = mutableListOf<Ticket>()
            for (item in it.children) {
                val itemTicket = item.getValue<Ticket>()!!
                ticketsArray.add(itemTicket)
            }
            ticketsArray.remove(ticket)
            databaseReference.child("tickets").setValue(ticketsArray).addOnSuccessListener {
                databaseReference.child("routes").child(ticket.routeId.toString()).get()
                    .addOnSuccessListener { routes ->
                        databaseReference.child("routes").child(ticket.routeId.toString())
                            .child("ticketsLeft").setValue(
                                (routes.getValue<Route>()?.ticketsLeft.toString()
                                    .toInt() + ticket.seats.toString().toInt()).toString()
                            ).addOnSuccessListener {
                                databaseReference.child("users").child(Firebase.auth.uid.toString())
                                    .child("tickets").get().addOnSuccessListener { userTickets ->
                                    var tickets = userTickets.getValue<List<String>>()
                                    tickets = if (tickets == null) mutableListOf()
                                    else tickets as MutableList<String>
                                    tickets.remove(ticket.id.toString())
                                    val childUpdates = hashMapOf(
                                        "/routes/${ticket.routeId}/ticketsLeft" to (routes.getValue<Route>()?.ticketsLeft.toString()
                                            .toInt() + ticket.seats.toString().toInt()).toString(),
                                        "/users/${Firebase.auth.currentUser?.uid}/tickets" to tickets
                                    )
                                    try {
                                        databaseReference.updateChildren(childUpdates)
                                    } catch (e: Exception) {
                                        Log.e("Cancel order", e.toString())
                                    }
                                }
                                loadTickets()
                            }
                    }
            }
        }
    }
}