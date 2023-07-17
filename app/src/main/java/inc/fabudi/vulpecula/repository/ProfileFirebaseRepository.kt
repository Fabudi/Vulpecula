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
    fun logout() = Firebase.auth.signOut()

    private val databaseReference = Firebase.database.reference

    var ticketsQuantity = MutableLiveData<String>()
    var distance = MutableLiveData<String>()
    var discount = MutableLiveData<String>()
    var email = MutableLiveData<String>()
    var contactPhone = MutableLiveData<String>()
    var fullName = MutableLiveData<String>()
    var userPhone = MutableLiveData<String>()

    init {
        refreshData()
    }

    private fun refreshData() {
        databaseReference.child("contactInfo").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contactInfo = snapshot.getValue<ContactInfo>()
                email.postValue(contactInfo?.email.toString())
                contactPhone.postValue(contactInfo?.phone.toString())
                Log.d("ContactInfo", contactInfo.toString())
                databaseReference.child("users").child(Firebase.auth.uid.toString()).get()
                    .addOnSuccessListener {
                        val user = it.getValue<User>()
                        fullName.postValue("${user?.name} ${user?.lastname}")
                        userPhone.postValue(Firebase.auth.currentUser?.phoneNumber)
                        ticketsQuantity.postValue(user?.tickets?.size.toString())
                        databaseReference.child("tickets").get().addOnSuccessListener { ticketsDb ->
                            val tickets = mutableListOf<Ticket>()
                            for (item in ticketsDb.children) {
                                val ticket = item.getValue<Ticket>()
                                if (user?.tickets?.contains(ticket?.id.toString()) == true) tickets.add(
                                    ticket!!
                                )
                            }
                            var totalDistance = 0
                            tickets.forEach { ticket ->
                                totalDistance += ticket.distance.toString().toInt()
                            }
                            distance.postValue(totalDistance.toString())
                            discount.postValue("${(totalDistance / 1000)}%")
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ContactInfo", error.toString())
            }

        })
    }
}