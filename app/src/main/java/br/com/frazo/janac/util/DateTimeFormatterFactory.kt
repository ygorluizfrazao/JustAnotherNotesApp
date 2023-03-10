package br.com.frazo.janac.util

import android.content.Context
import br.com.frazo.janac.R
import java.time.format.DateTimeFormatter

class DateTimeFormatterFactory(private val context: Context? = null) {

    fun datePattern(): DateTimeFormatter {
        context?.let {
            val datePattern = context.getString(R.string.date_format)
            if (datePattern == "default")
                return@let
            return DateTimeFormatter.ofPattern(datePattern)
        }
        return DateTimeFormatter.ISO_LOCAL_DATE
    }
}