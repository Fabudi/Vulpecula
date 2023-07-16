package inc.fabudi.vulpecula.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.fabudi.vulpecula.repository.AuthFirebaseRepository

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthFirebaseRepository()

    fun isLoggedIn() = repository.isLoggedIn()

    fun login(phoneNumber: String) = repository.sendCodeToPhone(phoneNumber)


    fun writeUserToDatabase(name: String, lastname: String) =
        repository.writeUserToDatabase(name, lastname)


    fun completeLogin(code: String) = repository.sendCodeToVerify(code)


    val codeSent = repository.codeSent
    val newUser = repository.newUser


    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return LoginViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}