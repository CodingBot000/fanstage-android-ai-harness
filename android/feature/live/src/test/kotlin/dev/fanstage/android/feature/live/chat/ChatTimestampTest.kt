package dev.fanstage.android.feature.live.chat

import java.text.DateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChatTimestampTest {
    @Test fun `UTC server instant is formatted in requested locale and zone`() {
        val instant = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US).apply {
            clear(); set(2026, Calendar.NOVEMBER, 5, 23, 30, 0)
        }.time
        for (locale in listOf(Locale.US, Locale.KOREA)) {
            val zone = TimeZone.getTimeZone("Asia/Seoul")
            val expected = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).apply { timeZone = zone }.format(instant)
            assertEquals(expected, formatChatTimestamp("2026-11-05T23:30:00.123456789Z", locale, zone))
        }
    }

    @Test fun `absent invalid or non UTC timestamps are omitted`() {
        val utc = TimeZone.getTimeZone("UTC")
        for (value in listOf(null, "", "2026-02-30T12:00:00Z", "2026-11-05T25:00:00Z", "2026-11-05T12:00:00+09:00", "not-a-date")) {
            assertNull(formatChatTimestamp(value, Locale.US, utc))
        }
    }
}
