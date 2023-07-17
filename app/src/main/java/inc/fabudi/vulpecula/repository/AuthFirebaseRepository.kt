package inc.fabudi.vulpecula.repository

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import inc.fabudi.vulpecula.domain.User
import java.util.concurrent.TimeUnit


class AuthFirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val databaseReference = Firebase.database.reference
    var user: MutableLiveData<FirebaseUser> = MutableLiveData()
    private var loggedOut: MutableLiveData<Boolean> = MutableLiveData()
    var wrongCode: MutableLiveData<Boolean> = MutableLiveData()
    var codeSent = ObservableBoolean()
    var newUser = ObservableBoolean()

    private var storedVerificationId: MutableLiveData<String> = MutableLiveData()
    private var resendToken: MutableLiveData<PhoneAuthProvider.ForceResendingToken> =
        MutableLiveData()

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {}
                is FirebaseTooManyRequestsException -> {}
                is FirebaseAuthMissingActivityForRecaptchaException -> {}
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            storedVerificationId.postValue(verificationId)
            resendToken.postValue(token)
            codeSent.set(true)
        }
    }


    init {
        if (auth.currentUser != null) {
            try {
                auth.currentUser?.getIdToken(true)
                user.postValue(auth.currentUser)
                loggedOut.postValue(false)
            } catch (e: FirebaseAuthInvalidUserException) {
                logOut()
            }
        }
    }

    fun isLoggedIn() = auth.currentUser != null

    fun sendCodeToPhone(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS).setCallbacks(callbacks).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun sendCodeToVerify(code: String) {
        signInWithPhoneAuthCredential(
            PhoneAuthProvider.getCredential(
                storedVerificationId.value.toString(), code
            )
        )
    }

    fun logOut() {
        auth.signOut()
        loggedOut.postValue(true)
    }

    fun writeUserToDatabase(name: String, lastname: String) {
        val user = User(auth.uid, name, lastname)
        databaseReference.child("users").child(user.uid.toString()).setValue(user)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                newUser.set(task.result.additionalUserInfo?.isNewUser == true)
            } else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    wrongCode.postValue(true)
                }
            }
        }
    }

}