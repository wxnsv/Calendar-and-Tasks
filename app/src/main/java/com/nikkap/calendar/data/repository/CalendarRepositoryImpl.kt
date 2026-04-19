package com.nikkap.calendar.data.repository

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.syncEntities
import com.nikkap.calendar.core.utils.toIsoDateWithoutSeconds
import com.nikkap.calendar.data.local.dao.CalendarDao
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.mapper.changePendingAction
import com.nikkap.calendar.data.mapper.markAsSynchronized
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CalendarRepositoryImpl(
    private val api: CalendarApi,
    private val dao: CalendarDao,
    private val userPrefRepository: UserPreferencesRepository
) : CalendarRepository {
    override val allEvents: Flow<List<Event>> = dao.getNonDeleteEvents()
        .map { entities ->
            entities.map { it.toEvent() }
        }
    override val allBirthdays: Flow<List<Birthday>> = dao.getNonDeleteBirthdays()
        .map { entities ->
            entities.map { it.toBirthday() }
        }

    override suspend fun getEvent(id: String): Event {
        return dao.getEvent(id)!!.toEvent()
    }

    override suspend fun getBirthday(id: String): Birthday {
        return dao.getBirthday(id).toBirthday()
    }

    override suspend fun saveEvent(event: Event) {
        dao.insertEvent(event.toEventEntity().changePendingAction(PendingActions.INSERT))
        api.createEvent(
            event = event.toEventDto(),
        )
    }

    override suspend fun updateBirthday(birthday: Birthday) {
        dao.updateBirthday(birthday.toBirthdayEntity().changePendingAction(PendingActions.UPDATE))
        api.updateBirthday(
            birthday = birthday.toBirthdayDto(),
            birthdayId = birthday.id!!
        )
    }

    override suspend fun updateEvent(event: Event) {
        dao.updateEvent(event.toEventEntity().changePendingAction(PendingActions.UPDATE))
        api.updateEvent(
            event = event.toEventDto(),
            eventId = event.id!!
        )
    }

//    TODO fun deleteEvent

    override suspend fun saveBirthday(birthday: Birthday) {
        dao.insertBirthday(birthday.toBirthdayEntity().changePendingAction(PendingActions.INSERT))
        api.createBirthday(birthday.toBirthdayDto())
    }

    override suspend fun syncCalendar(): Result<Unit> = try {
        val calendarSyncTime =
            userPrefRepository.calendarSyncTime.first()?.toIsoDateWithoutSeconds()
        val timeMin = userPrefRepository.calendarTimeMin.first()

        coroutineScope {
            val eventPendingResult = async { eventsPendingSync() }
            val birthdayPendingResult = async { birthdaysPendingSync() }

            eventPendingResult.await()
            birthdayPendingResult.await()

            val eventsResult = async {

                val responseEvents = api.getEvents(
                    timeMin,
                    updatedMin = calendarSyncTime,
                    singleEvents = true,
                )

                val remoteEvents = responseEvents.body()?.items?.map {
                    if (!it.deleted) it.toEventEntity() else it.toEventEntity()
                        .changePendingAction(PendingActions.DELETE)
                } ?: emptyList()

                if (responseEvents.code() != 200) {
                    handleErrorCode(responseEvents.code())
                    return@async Result.failure(Exception("Failed to sync events and birthdays"))
                }

                if (remoteEvents.isNotEmpty()) {
                    syncEntities(
                        remoteEntities = remoteEvents,
                        getLocalEntities = { dao.getNonDeleteEvents().first() },
                        deleteEntitiesByIds = { dao.deleteEventsByIds(it) },
                        insertEntities = { dao.insertEvents(it) }
                    )
                } else Result.success(Unit)
            }
            val birthdayResult = async {

                val responseBirthdays = api.getBirthdays(
                    timeMin,
                    updatedMin = calendarSyncTime,
                )

                if (responseBirthdays.code() != 200) {
                    handleErrorCode(responseBirthdays.code())
                    return@async Result.failure(Exception("Failed to sync events and birthdays"))
                }

                val remoteBirthdays = responseBirthdays.body()?.items?.map {
                    if (!it.deleted) it.toBirthdayEntity() else it.toBirthdayEntity()
                        .changePendingAction(PendingActions.DELETE)
                } ?: emptyList()

                if (remoteBirthdays.isNotEmpty()) {
                    syncEntities(
                        remoteEntities = remoteBirthdays,
                        getLocalEntities = { dao.getNonDeleteBirthdays().first() },
                        deleteEntitiesByIds = { dao.deleteBirthdaysByIds(it) },
                        insertEntities = { dao.insertBirthdays(it) }
                    )
                } else Result.success(Unit)
            }

            if (eventsResult.await().isSuccess && birthdayResult.await().isSuccess) {
                userPrefRepository.updateEventSyncTime()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync events and birthdays"))
            }

        }
    } catch (e: Exception) {
        Result.failure(Exception("Failed to sync events and birthdays with ${e.message} message"))
    }

    private suspend fun handleErrorCode(code: Int) {
        when (code) {
            410 -> {
                userPrefRepository.clearLastCalendarSyncTime()
            }
            // TODO

        }
    }


    private suspend fun birthdaysPendingSync() {
        val pendingEntities = dao.getNonDeleteBirthdays().first()
        coroutineScope {
            pendingEntities.map { entity ->
                async {
                    when (entity.pendingAction) {
                        PendingActions.DELETE -> {
                            if (api.deleteItem(eventId = entity.id).isSuccessful) dao.deleteBirthday(
                                entity
                            )
                        }

                        PendingActions.UPDATE -> {
                            val result = api.updateBirthday(
                                birthdayId = entity.id,
                                birthday = entity.toBirthdayDto()
                            )
                            if (result.isSuccessful
                            ) {
                                dao.updateBirthday(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                            }
                        }

                        PendingActions.INSERT -> {
                            val result = api.createBirthday(entity.toBirthdayDto())
                            if (result.isSuccessful) {
                                dao.insertBirthday(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                            }
                        }

                        PendingActions.NONE -> {}
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun eventsPendingSync() {
        val pendingEntities = dao.getPendingEvents().first()
        coroutineScope {
            pendingEntities.map { entity ->
                async {
                    when (entity.pendingAction) {
                        PendingActions.DELETE -> if (api.deleteItem(eventId = entity.id).isSuccessful) dao.deleteEvent(
                            entity
                        )

                        PendingActions.UPDATE -> {
                            val result = api.updateEvent(
                                eventId = entity.id,
                                event = entity.toEventDto()
                            )
                            if (result.isSuccessful) {
                                dao.updateEvent(entity.markAsSynchronized(parseIsoDate(result.body()?.updated)))
                            }
                        }

                        PendingActions.INSERT -> {
                            val result = api.createEvent(entity.toEventDto())
                            if (result.isSuccessful) dao.updateEvent(
                                entity.markAsSynchronized(parseIsoDate(result.body()?.updated))
                            )
                        }

                        PendingActions.NONE -> {}
                    }
                }
            }.awaitAll()
        }
    }
}