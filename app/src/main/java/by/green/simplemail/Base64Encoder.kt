package by.green.simplemail

import java.nio.charset.Charset

private const val C_KOI8R_PREFIX = "=?KOI8-R?B?"
private const val C_KOI8R_SUFFIX = "==?="
private const val C_KOI8R_SUFFIX2 = "=?="



fun String.decodeBase64(): String {
    var s: String? = null
    lateinit var charset: Charset
    if (this.startsWith(C_KOI8R_PREFIX, ignoreCase = true)) {
        charset = Charset.forName("KOI8-R")
        s = this.removePrefix(C_KOI8R_PREFIX)
        s = s.removeSuffix(C_KOI8R_SUFFIX)
        s = s.removeSuffix(C_KOI8R_SUFFIX2)

    } else {
        return this
      }

    return android.util.Base64.decode(s,0).toString(charset)
}
