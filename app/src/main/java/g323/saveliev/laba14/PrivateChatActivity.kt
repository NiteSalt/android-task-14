package g323.saveliev.laba14

import android.os.Bundle
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
import java.net.ServerSocket
import java.net.URL


class PrivateChatActivity : AppCompatActivity() {
    private var chatId: Long = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var contentField: TextInputEditText
    private lateinit var adapter: MessageAdapter

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
        recyclerView.layoutManager = LinearLayoutManager(this)

        onMessageUpdate()
        startListening()
    }

    private var getMessageJob: Job? = null
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
                        adapter.notifyItemInserted(messages.length() - 1)
                        recyclerView.scrollToPosition(messages.length() - 1)
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
                connection.doOutput = true
                connection.requestMethod = "POST"

                connection.outputStream.bufferedWriter().use { writer ->
                    writer.write(content)
                    writer.flush()
                }

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

    private var serverSocket: ServerSocket? = null
    private var listenerJob: Job? = null
    private fun stopListening() {
        listenerJob?.cancel()
        serverSocket?.close()
        serverSocket = null
    }

    private fun startListening() {
        stopListening()

        listenerJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(7314)
                while (true) {
                    serverSocket?.accept()?.use {
                        onMessageUpdate()
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