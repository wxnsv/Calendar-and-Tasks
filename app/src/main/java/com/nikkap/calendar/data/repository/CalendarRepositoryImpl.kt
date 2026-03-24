package com.nikkap.calendar.data.repository

import com.nikkap.calendar.data.local.dao.CalendarDao
import com.nikkap.calendar.data.mapper.toBirthday
import com.nikkap.calendar.data.mapper.toBirthdayDto
import com.nikkap.calendar.data.mapper.toBirthdayEntity
import com.nikkap.calendar.data.mapper.toEvent
import com.nikkap.calendar.data.mapper.toEventDto
import com.nikkap.calendar.data.mapper.toEventEntity
import com.nikkap.calendar.data.remote.api.CalendarApi
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.time.Clock

class CalendarRepositoryImpl(
    private val api: CalendarApi,
    private val dao: CalendarDao,
    private val userPrefRepository: UserPreferencesRepository
) : CalendarRepository {
    override val allEvents: Flow<List<Event>> = dao.getAllEvents()
        .map { entities ->
            entities.map { it.toEvent() }
        }
    override val allBirthdays: Flow<List<Birthday>> = dao.getAllBirthdays()
        .map { entities ->
            entities.map { it.toBirthday() }
        }

    override suspend fun getEvent(id: String): Event {
        return dao.getEvent(id).toEvent()
    }

    override suspend fun getBirthday(id: String): Birthday {
        return dao.getBirthday(id).toBirthday()
    }

    override suspend fun saveEvent(event: Event) {
        dao.insertEvent(event.toEventEntity())
        api.createEvent(
            event = event.toEventDto(),
        )
    }

    override suspend fun updateBirthday(birthday: Birthday) {
        dao.updateBirthday(birthday.toBirthdayEntity())
        api.updateBirthday(
            birthday = birthday.toBirthdayDto(),
            birthdayId = birthday.id!!
        )
    }

    override suspend fun updateEvent(event: Event) {
        dao.updateEvent(event.toEventEntity())
        api.updateEvent(
            event = event.toEventDto(),
            eventId = event.id!!
        )
    }

    override suspend fun saveBirthday(birthday: Birthday) {
        dao.insertBirthday(birthday.toBirthdayEntity())
        api.createBirthday(birthday.toBirthdayDto())
    }

    override suspend fun haveLocalData(): Boolean {
        return dao.getCount() > 0
    }


    override suspend fun syncCalendar(): Result<Unit> = try {
        val timeMin = Clock.System.now()
            .minus(3, DateTimeUnit.YEAR, TimeZone.UTC)
            .toString()
        val eventsResult = syncEvents(timeMin)
        val birthdayResult = syncBirthdays(timeMin)
        if (eventsResult.isSuccess) {
            userPrefRepository.updateEventSyncTime()
        }
        if (birthdayResult.isSuccess) {
            userPrefRepository.updateBirthdaySyncTime()
        }
        eventsResult
        // TODO
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun syncEvents(timeMin: String): Result<Unit> {
        val responseEvents = api.getEvents(
            timeMin = timeMin,
            type = "default",
            singleEvents = true,
        )

        if (responseEvents.isSuccessful) {
            val entities = responseEvents.body()?.items?.map { it.toEventEntity() } ?: emptyList()
            dao.insertEvents(entities)
            return Result.success(Unit)
        } else {
            return Result.failure(Exception("Error: ${responseEvents.code()} when trying to sync Events"))
        }
    }

    private suspend fun syncBirthdays(timeMin: String): Result<Unit> {
        val responseBirthdays = api.getBirthdays(
            timeMin = timeMin,
            type = "birthday",
        )

        if (responseBirthdays.isSuccessful) {
            val entities =
                responseBirthdays.body()?.items?.map { it.toBirthdayEntity() } ?: emptyList()
            dao.insertBirthdays(entities)
            return Result.success(Unit)
        } else {
            return Result.failure(Exception("Error: ${responseBirthdays.code()} when trying to sync Birthdays"))
        }
    }
}