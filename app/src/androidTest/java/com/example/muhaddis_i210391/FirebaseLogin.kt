
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseAuthTest {

    private lateinit var auth: FirebaseAuth

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        FirebaseApp.initializeApp(context)

        // Set up the Firebase Auth emulator instance
        auth = Firebase.auth
    }

    @Test
    fun testFirebaseSignInWithExistingUser() {
        // Sign in with an existing user account
        auth.signInWithEmailAndPassword("existing_user@example.com", "password123")
            .addOnCompleteListener { signInTask ->
                assertTrue(signInTask.isSuccessful)
            }
    }
}
