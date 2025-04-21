package g323.saveliev.laba14

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import java.net.*


class MainActivity : AppCompatActivity() {
    private lateinit var ipAddressBox: TextInputEditText
    private lateinit var usernameBox: TextInputEditText

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ipAddressBox = findViewById(R.id.ipEditText)
        usernameBox = findViewById(R.id.usernameInput)
    }

    private fun onLogin() {
        val intent = Intent(this, UsersActivity::class.java)
        startActivity(intent)
    }

    fun onConnectClick(view: View) {
        Thread {
            try {
                val url = URL("http://${ipAddressBox.text}:7314/api/users/update/${usernameBox.text}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connectTimeout = 1200

                // For empty POST body
                connection.outputStream.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread {
                        Toast.makeText(this, "Удалось подключиться", Toast.LENGTH_SHORT).show()
                        ServerIP = ipAddressBox.text.toString()
                        onLogin()
                    }
                    Log.v("MAIN", "200 OK")
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Не удалось подключиться", Toast.LENGTH_SHORT).show()
                    }
                    Log.v("MAIN", "$responseCode NOT OK")
                }
            }
            catch (e: UnknownHostException) {
                runOnUiThread {
                    Toast.makeText(this, "Неизвестный хост", Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: SocketTimeoutException) {
                runOnUiThread {
                    Toast.makeText(this, "Не удалось за валидное время подключится до сервера", Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    companion object {
        lateinit var ServerIP: String
    }
}