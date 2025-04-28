package g323.saveliev.laba14

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class PrivateChatActivity : AppCompatActivity() {
    private var chatId: Long = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var contentField: TextInputEditText
    private lateinit var adapter: MessageAdapter
    private lateinit var updaterThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_private_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        chatId = intent.getLongExtra("chatId", 0)
        OtherIp = intent.getStringExtra("otherIp").toString()
        recyclerView = findViewById(R.id.recyclerView)
        contentField = findViewById(R.id.messageInput)

        adapter = MessageAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        onMessageUpdate()
        updaterThread = Thread {
            while (!this@PrivateChatActivity.isDestroyed) {
                Thread.sleep(300)
                onMessageUpdate()
            }
        }
        updaterThread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private var getMessageJob: Job? = null
    @SuppressLint("NotifyDataSetChanged")
    private fun onMessageUpdate() {
        getMessageJob?.cancel()

        getMessageJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://${MainActivity.ServerIP}:7314/api/chat/get_messages/$chatId")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                when (connection.responseCode) {
                    200 -> {
                        val messages = JSONArray(Utilities.readStreamString(connection.inputStream))
                        runOnUiThread {
                            adapter.clear()

                            for (i in 0..<messages.length()) {
                                val messageJson = messages.getJSONObject(i)

                                val message = Message(
                                    messageJson.getLong("id"),
                                    messageJson.getJSONObject("sender").getString("ipAddress"),
                                    Utilities.formatUtcToTime(messageJson.getString("date")),
                                    messageJson.getString("content")
                                )

                                adapter.add(message)
                            }

                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var sendJob: Job? = null
    fun onSendButtonClick(view: View) {
        val content = contentField.text.toString()

        if (content.isBlank()) {
            return
        }

        sendJob?.cancel()

        sendJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://${MainActivity.ServerIP}:7314/api/chat/send_message/$chatId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Accept", "*/*")
                connection.setRequestProperty("Content-Type", "text/html; charset=utf-8")
                connection.doOutput = false
                connection.connectTimeout = 500
                connection.readTimeout = 500

                val input: ByteArray = content.toByteArray(Charsets.UTF_8)
                connection.outputStream.write(input)
                connection.outputStream.flush()
                Log.d("ServerInponse", input.toString())

                Log.w("ServerResponse", Utilities.readStreamString(connection.inputStream))

                when (connection.responseCode) {
                    200 -> {
                        runOnUiThread {
                            contentField.text?.clear()
                            Toast.makeText(this@PrivateChatActivity, "Сообщение отправлено", Toast.LENGTH_SHORT).show()
                        }
                    }
                    404 -> {
                        runOnUiThread {
                            Toast.makeText(this@PrivateChatActivity, "Чат не существует", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        var OtherIp: String = ""
    }
}