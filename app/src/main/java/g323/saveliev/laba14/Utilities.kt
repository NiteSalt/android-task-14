package g323.saveliev.laba14

import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
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
            val cleanedInput = utcDateTimeString.substringBefore(".") + "Z"

            val instant = Instant.parse(cleanedInput)

            val localTime = instant.atZone(ZoneId.systemDefault())

            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

            return localTime.format(formatter)
        }
    }
}