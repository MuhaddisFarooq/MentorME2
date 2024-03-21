import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirestoreEmulatorTest {

    companion object {
        private var firebaseInitialized = false
    }
    @Before
    fun setup() {
        // Check if FirebaseApp with name "DEFAULT" already exists
        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            val context: Context = ApplicationProvider.getApplicationContext()
            val options = FirebaseOptions.Builder()
                .setProjectId("mentorme-a5a49")
                .setApplicationId("com.example.muhaddis_i210391")
                .setDatabaseUrl("https://mentorme-a5a49>.firebaseio.com/")
                .build()

            FirebaseApp.initializeApp(context, options)
        }

        // Point Firestore to the emulator
        val firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)
    }

    @Test
    fun testAddDocumentToFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val latch = CountDownLatch(1)

        val user = hashMapOf("name" to "John Doe", "email" to "john@example.com")

        firestore.collection("users").add(user)
            .addOnCompleteListener { task ->
                assertTrue(task.isSuccessful)
                latch.countDown() // Decrement the count of the latch, releasing all waiting threads
            }

        // Wait for the async operation to complete or timeout
        latch.await(5, TimeUnit.SECONDS)
    }
}
