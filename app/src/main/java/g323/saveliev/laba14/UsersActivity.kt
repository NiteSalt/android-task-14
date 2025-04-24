package g323.saveliev.laba14

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread


class UsersActivity : AppCompatActivity() {
    private lateinit var listview: ListView
    private lateinit var adapter: UsersAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_users)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        listview = findViewById(R.id.usersListView)
        adapter = UsersAdapter(this)
        listview.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener { onUsersLoading() }
        listview.setOnItemClickListener(::onUserClick)
        onUsersLoading()
    }

    private var userJob: Job? = null
    private fun onUserClick(adapterView: AdapterView<*>?, view: View, int: Int, long: Long) {
        val user = adapter.getUser(int)

        userJob?.cancel()

        userJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://${MainActivity.ServerIP}:7314/api/chat/get/${user.ipAddress}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                when (connection.responseCode) {
                    200 -> {
                        val jsonResponse = JSONObject(Utilities.readStreamString(connection.inputStream))
                        val privateChatId = jsonResponse.getLong("id")

                        runOnUiThread {
                            val intent = Intent(this@UsersActivity, PrivateChatActivity::class.java)
                            intent.putExtra("chatId", privateChatId)
                            startActivity(intent)
                        }
                    }
                    400 -> {
                        runOnUiThread {
                            Toast.makeText(this@UsersActivity, "Вы не можете писать сообщения самому себе!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    404 -> {
                        runOnUiThread {
                            Toast.makeText(this@UsersActivity, "Пользователь не найден!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                runOnUiThread {
                    swipeRefreshLayout.isRefreshing = false
                    inProcess = false
                }
            }
        }
    }

    private var inProcess: Boolean = false

    private fun onUsersLoading() {
        runOnUiThread {
            swipeRefreshLayout.isRefreshing = true
        }

        if (!inProcess) {
            thread {
                inProcess = true
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
                finally {
                    runOnUiThread {
                        swipeRefreshLayout.isRefreshing = false
                        inProcess = false
                    }
                }
            }
        }
    }
}