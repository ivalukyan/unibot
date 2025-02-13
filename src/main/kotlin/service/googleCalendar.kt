import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.client.util.DateTime
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object GoogleCalendarService {
    private const val APPLICATION_NAME = "РКБО-03-22"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    private val SCOPES = listOf(CalendarScopes.CALENDAR_READONLY)

    @Throws(Exception::class)
    private fun getCredentials(): GoogleCredentials {

        val credentialsStream = FileInputStream("app/resources/cred.json")
        return GoogleCredentials.fromStream(credentialsStream)
            .createScoped(SCOPES)
    }

    @Throws(Exception::class)
    fun getCalendarService(): Calendar {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credentials = getCredentials()
        return Calendar.Builder(httpTransport, JSON_FACTORY, HttpCredentialsAdapter(credentials))
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
}

@Throws(Exception::class)
fun getEventsForDate(service: Calendar, date: String): String {
    var res = ""
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val localDate = LocalDate.parse(date, formatter)
    val calendarId = "d78a741606c0a12524e032aee4b4df77d502e7210d8830a84f7c8ea71f36e305@group.calendar.google.com"

    // Преобразуем LocalDate в ZonedDateTime и форматируем в строку RFC 3339
    val timeMin = DateTime(
        ZonedDateTime.of(localDate.atStartOfDay(), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    )
    val timeMax = DateTime(
        ZonedDateTime.of(localDate.atTime(23, 59, 59), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    )

    val events = service.events().list(calendarId)
        .setTimeMin(timeMin)
        .setTimeMax(timeMax)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute()

    if (events.items.isEmpty()) {
        val formDate = date.split("-")
        res = "На ${formDate[2]}-${formDate[1]}-${formDate[0]} событий нет."
    } else {
        events.items.forEach { event ->
            val startTime = event.start.dateTime ?: event.start.date
            val endTime = event.end.dateTime ?: event.end.date

            // Преобразуем строки в LocalDateTime или LocalDate
            val startDateTime = if (event.start.dateTime != null) {
                LocalDateTime.parse(startTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
            } else {
                LocalDate.parse(startTime.toString(), DateTimeFormatter.ISO_DATE).atStartOfDay()
            }

            val endDateTime = if (event.end.dateTime != null) {
                LocalDateTime.parse(endTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
            } else {
                LocalDate.parse(endTime.toString(), DateTimeFormatter.ISO_DATE).atStartOfDay()
            }

            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) // или FormatStyle.SHORT/LONG
            val formattedStartTime = startDateTime.format(formatter)
            val formattedEndTime = endDateTime.format(formatter)
            val description = event.description ?: "Описание отсутствует"

            res = res + "Название события: ${event.summary}\nОписание: ${description}\n" +
                    "Время: (с $formattedStartTime по $formattedEndTime)\n\n"
        }
    }

    return res
}