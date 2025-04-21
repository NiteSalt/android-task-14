package g323.saveliev.laba14

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import kotlin.concurrent.thread


class UsersActivity : AppCompatActivity() {
    private lateinit var listview: ListView
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_users)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listview = findViewById<ListView>(R.id.usersListView)
        adapter = UsersAdapter(this)
        listview.adapter = adapter

        thread {
            try {
                val url = URL("http://${MainActivity.ServerIP}:7314/api/users/get_all")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == 200) {
                    val jsonResponse = JSONArray(Utilities.readStreamString(connection.inputStream))
                    runOnUiThread {
                        val array = arrayOfNulls<User>(jsonResponse.length())

                        for (i in 0..<jsonResponse.length()) {
                            array[i] = User(
                                (jsonResponse[i] as JSONObject).getString("ipAddress"),
                                (jsonResponse[i] as JSONObject).getString("username")
                            )
                        }

                        adapter.fill(array)
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}