package g323.saveliev.laba14

import java.io.InputStream


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
    }
}