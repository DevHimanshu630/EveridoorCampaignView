import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException
import android.util.Log

object SocketHandler {
    private const val SERVER_URL = "https://qdp72jc1-4000.inc1.devtunnels.ms"
    private lateinit var msocket: Socket

    @Synchronized
    fun setSocket() {
        try {
            val options = IO.Options()
            options.forceNew = true
            msocket = IO.socket(SERVER_URL, options)
        } catch (e: URISyntaxException) {
            Log.e("SocketHandler", "Error creating socket: $e")
        }
    }

    @Synchronized
    fun establishConnection() {
        msocket.connect()
        msocket.on(Socket.EVENT_CONNECT) {
            Log.e("SocketHandler", "Connection Established")

        }
    }

    @Synchronized
    fun closeConnection() {
        msocket.disconnect()
        Log.e("SocketHandler", "Connection DISCONNECT")
    }

    @Synchronized
    fun getSocket(): Socket {
        return msocket
    }
}
