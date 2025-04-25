package g323.saveliev.laba14

import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class Utilities {
    companion object {
        fun readStreamString(inputStream: InputStream): String {
            val buf = ByteArray(1024)
            val res = StringBuilder()

            while (true) {
                val len = inputStream.read(buf, 0, buf.size)
                if (len < 0) break
                res.append(String(buf, 0, len))
            }

            return res.toString()
        }

        fun formatUtcToTime(utcDateTimeString: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            val date = inputFormat.parse(utcDateTimeString)

            val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return outputFormat.format(date!!)
        }
    }
}