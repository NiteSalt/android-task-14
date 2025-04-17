package g323.saveliev.laba14

import android.annotation.SuppressLint
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView

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

        textView = findViewById(R.id.textView)

        textView.text = getBroadcastAddress().hostName
        return
        thread {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                ServerSocket(7314, 1, InetAddress.getByName("0.0.0.0")).use { listener ->
                    val udpSocket = DatagramSocket(7314)
                    udpSocket.broadcast = true

                    var i = 0
                    val wait: Future<Socket> =
                        Executors.newSingleThreadExecutor().submit<Socket> { listener.accept() }
                    while (true) { // Try to locate
                        val buffer = ByteArray(0)
                        val packet = DatagramPacket(
                            buffer, buffer.size,
                            InetAddress.getByName("255.255.255.255"), 7314
                        )
                        udpSocket.send(packet)

                        runOnUiThread {
                            textView.text = "Try to locate server #" + (++i)
                        }
                        Thread.sleep(1000)

                        if (wait.isDone) {
                            try {
                                val client: Socket = wait.get()
                                runOnUiThread {
                                    textView.text = "Connected from " + client.remoteSocketAddress
                                }
                                break
                            } catch (e: ExecutionException) {
                                e.printStackTrace()
                            }
                        }

                        if (i >= 10) {
                            runOnUiThread {
                                textView.text = "Server is offline"
                            }
                            System.err.println("Server is offline")
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    textView.text = e.message
                }
            }
        }
    }

    @Throws(IOException::class)
    fun getBroadcastAddress(): InetAddress {
        val dhcp = (getSystemService(WIFI_SERVICE) as WifiManager).dhcpInfo

        // handle null somehow
        val broadcast = (dhcp.ipAddress and dhcp.netmask) or dhcp.netmask.inv()
        val quads = ByteArray(4)
        for (k in 0..3) quads[k] = ((broadcast shr k * 8) and 0xFF).toByte()
        return InetAddress.getByAddress(quads)
    }
}