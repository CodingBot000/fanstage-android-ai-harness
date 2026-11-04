package dev.fanstage.android.feature.live.chat

import java.text.DateFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Server timestamps are UTC instants; invalid or absent timestamps are not displayed. */
internal fun formatChatTimestamp(value: String?, locale: Locale, zone: TimeZone): String? {
    if (value == null || !value.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})?Z"))) return null
    val seconds = value.substring(0, 19)
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val position = ParsePosition(0)
    val instant = parser.parse(seconds, position) ?: return null
    if (position.index != seconds.length) return null
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).apply { timeZone = zone }.format(instant)
}
