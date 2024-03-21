import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FirebaseDatabaseTest {

    private lateinit var database: DatabaseReference

    @Before
    fun setup() {
        // Initialize Firebase
        val context = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.initializeApp(context)

        // Set up the Realtime Database emulator instance
        database = FirebaseDatabase.getInstance().reference
    }

    @Test
    fun testRealtimeDatabaseUpdate() {
        val initialValue = "Initial value"
        val updatedValue = "Updated value"

        val latch = CountDownLatch(1)

        // Write initial value to the database
        database.child("testNode").setValue(initialValue)
            .addOnCompleteListener { writeTask ->
                assertTrue(writeTask.isSuccessful)

                // Perform an update operation
                database.child("testNode").setValue(updatedValue)
                    .addOnCompleteListener { updateTask ->
                        assertTrue(updateTask.isSuccessful)

                        // Read the value back to verify the update
                        database.child("testNode").get().addOnCompleteListener { readTask ->
                            assertTrue(readTask.isSuccessful)
                            assertEquals(updatedValue, readTask.result?.value as String)
                            latch.countDown()
                        }
                    }
            }

        latch.await(5, TimeUnit.SECONDS)
    }
}
