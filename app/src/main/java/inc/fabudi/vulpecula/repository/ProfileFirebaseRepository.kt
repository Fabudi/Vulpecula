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
import inc.fabudi.vulpecula.domain.ContactInfo
import inc.fabudi.vulpecula.domain.Ticket
import inc.fabudi.vulpecula.domain.User

class ProfileFirebaseRepository {

    private val databaseReference = Firebase.database.reference
    var ticketsQuantity = MutableLiveData<String>()
    var distance = MutableLiveData<String>()
    var discount = MutableLiveData<String>()
    var email = MutableLiveData<String>()
    var contactPhone = MutableLiveData<String>()
    var fullName = MutableLiveData<String>()
    var userPhone = MutableLiveData<String>()

    init {
        refreshContactInfo()
        refreshUser()
    }

    fun logout() = Firebase.auth.signOut()

    private fun refreshContactInfo() {
        databaseReference.child("contactInfo").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contactInfo = snapshot.getValue<ContactInfo>()!!
                email.postValue(contactInfo.email)
                contactPhone.postValue(contactInfo.phone)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ContactInfo", error.toString())
            }

        })
    }

    private fun refreshUser() {
        databaseReference.child("users").child(Firebase.auth.uid.toString()).get()
            .addOnSuccessListener {
                val user = it.getValue<User>()!!
                fullName.postValue("${user.name} ${user.lastname}")
                userPhone.postValue(Firebase.auth.currentUser?.phoneNumber)
                ticketsQuantity.postValue(user.tickets.size.toString())
                refreshStatistics(user.tickets)
            }
    }

    private fun refreshStatistics(ticketsIds: List<String>) {
        databaseReference.child("tickets").get().addOnSuccessListener { snapshot ->
            val tickets = snapshot.children
                .map { item -> item.getValue<Ticket>()!! }
                .filter { ticket -> (ticketsIds.contains(ticket.id)) }
            val totalDistance = tickets.sumOf { it.distance.toString().toInt() }
            distance.postValue(totalDistance.toString())
            discount.postValue("${totalDistance / 1000}%")
        }
    }
}