import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Events
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Paths
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object GoogleCalendarService {
    private const val APPLICATION_NAME = "Google Calendar API Kotlin"
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    private val SCOPES = listOf(CalendarScopes.CALENDAR_READONLY)

    @Throws(Exception::class)
    private fun getCredentials(): Credential {
        val credentialsPath = Paths.get("src/main/resources/cred.json").toFile()
        val inputStream = FileInputStream(credentialsPath)
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        val flow = GoogleAuthorizationCodeFlow.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            clientSecrets,
            SCOPES
        ).setDataStoreFactory(FileDataStoreFactory(File("tokens")))
            .setAccessType("offline")
            .build()

        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
    }

    @Throws(Exception::class)
    fun getCalendarService(): Calendar {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        return Calendar.Builder(httpTransport, JSON_FACTORY, getCredentials())
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
}

@Throws(Exception::class)
fun getEventsForDate(service: Calendar, date: String): Events {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val localDate = LocalDate.parse(date, formatter)

    val timeMin = DateTime(ZonedDateTime.of(localDate.atStartOfDay(), ZoneId.systemDefault()).toString())
    val timeMax = DateTime(ZonedDateTime.of(localDate.atTime(23, 59, 59), ZoneId.systemDefault()).toString())

    val events = service.events().list("primary")
        .setTimeMin(timeMin)
        .setTimeMax(timeMax)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute()

    if (events.items.isEmpty()) {
        println("На $date событий нет.")
    } else {
        println("События на $date:")
    }

    return events
}