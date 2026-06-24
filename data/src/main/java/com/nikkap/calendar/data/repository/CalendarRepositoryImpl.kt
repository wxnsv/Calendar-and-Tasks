package com.nikkap.calendar.data.repository

import com.nikkap.calendar.core.exceptions.NetworkException
import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.data.local.dao.CalendarDao
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.mapper.changePendingAction
import com.nikkap.calendar.data.mapper.markAsSynchronized
import com.nikkap.calendar.data.mapper.toBirthday
import com.nikkap.calendar.data.mapper.toBirthdayDto
import com.nikkap.calendar.data.mapper.toBirthdayEntity
import com.nikkap.calendar.data.mapper.toBirthdayUpdateDto
import com.nikkap.calendar.data.mapper.toEvent
import com.nikkap.calendar.data.mapper.toEventEntity
import com.nikkap.calendar.data.mapper.toEventUpdateDto
import com.nikkap.calendar.data.remote.api.CalendarApi
import com.nikkap.calendar.data.utils.localSyncEntities
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CalendarRepositoryImpl(
    private val api: CalendarApi,
    private val dao: CalendarDao,
    private val appScope: CoroutineScope
) : CalendarRepository {

    override fun getNonDeleteEvents(): Flow<List<Event>> {
        return dao.getNonDeleteEvents().map {
            it.map { it.toEvent() }
        }
    }

    override fun getNonDeleteBirthdays(): Flow<List<Birthday>> {
        return dao.getNonDeleteBirthdays().map {
            it.map { it.toBirthday() }
        }
    }

    override suspend fun getEvent(id: String): Event {
        return dao.getEvent(id)!!.toEvent()
    }

    override suspend fun getBirthday(id: String): Birthday {
        return dao.getBirthday(id).toBirthday()
    }

    override suspend fun saveEvent(event: Event) {
        appScope.launch {
            dao.insertEvent(event.toEventEntity().changePendingAction(PendingActions.INSERT))
        }
    }

    override suspend fun updateBirthday(birthday: Birthday) {
        appScope.launch {
            dao.updateBirthday(
                birthday.toBirthdayEntity().changePendingAction(PendingActions.UPDATE)
            )
        }
    }

    override suspend fun updateEvent(event: Event) {
        appScope.launch {
            dao.updateEvent(event.toEventEntity().changePendingAction(PendingActions.UPDATE))
        }
    }

    override suspend fun deleteEvent(id: String) {
        appScope.launch {
            dao.markAsDeleteEvent(id, System.currentTimeMillis())
        }
    }

    override suspend fun deleteBirthday(id: String) {
        appScope.launch {
            dao.markAsDeleteBirthday(id, System.currentTimeMillis())
        }
    }

    override fun clearAll() {
        appScope.launch {
            dao.clearBirthdays()
            dao.clearEvents()
        }
    }

    override suspend fun saveBirthday(birthday: Birthday) {
        appScope.launch {
            dao.insertBirthday(
                birthday.toBirthdayEntity().changePendingAction(PendingActions.INSERT)
            )
        }
    }

    override suspend fun syncCalendar(): Result<Unit> = try {

        coroutineScope {
            val eventsResult = async {

                val responseEvents = api.getEvents(
                    singleEvents = true,
                )

                val remoteEvents = responseEvents.body()?.items?.map {
                    if (!it.deleted && it.status != "cancelled") it.toEventEntity() else it.toEventEntity()
                        .changePendingAction(PendingActions.DELETE)
                } ?: emptyList()

                if (!responseEvents.isSuccessful) {
                    throw NetworkException.ServerException(
                        responseEvents.code(),
                        "Error when syncing birthdays"
                    )
                }

                if (remoteEvents.isNotEmpty()) {
                    localSyncEntities(
                        remoteEntities = remoteEvents,
                        getLocalEntities = { dao.getNonDeleteEvents().first() },
                        deleteEntitiesByIds = { dao.deleteEventsByIds(it) },
                        insertEntities = { dao.insertEvents(it) }
                    )
                }
            }
            val birthdayResult = async {

                val responseBirthdays = api.getBirthdays()

                if (!responseBirthdays.isSuccessful) {
                    throw NetworkException.ServerException(
                        responseBirthdays.code(),
                        "Error when syncing birthdays"
                    )
                }

                val remoteBirthdays = responseBirthdays.body()?.items?.map {
                    if (!it.deleted) it.toBirthdayEntity() else it.toBirthdayEntity()
                        .changePendingAction(PendingActions.DELETE)
                } ?: emptyList()

                if (remoteBirthdays.isNotEmpty()) {
                    localSyncEntities(
                        remoteEntities = remoteBirthdays,
                        getLocalEntities = { dao.getNonDeleteBirthdays().first() },
                        deleteEntitiesByIds = { dao.deleteBirthdaysByIds(it) },
                        insertEntities = { dao.insertBirthdays(it) }
                    )
                } else Result.success(Unit)
            }
            eventsResult.await()
            birthdayResult.await()
            pendingSync()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun pendingSync() {
        eventsPendingSync()
        birthdaysPendingSync()
    }

    private suspend fun birthdaysPendingSync() {
        val pendingEntities = dao.getPendingBirthdays().first()
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
                            val result = api.createBirthday(entity.toBirthdayUpdateDto())
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
                            entity.id
                        )

                        PendingActions.UPDATE -> {
                            val result = api.updateEvent(
                                eventId = entity.id,
                                event = entity.toEventUpdateDto()
                            )
                            if (result.isSuccessful) {
                                dao.updateEvent(entity.markAsSynchronized(parseIsoDate(result.body()?.updated)))
                            }
                        }

                        PendingActions.INSERT -> {
                            val dto = entity.toEventUpdateDto()
                            val result = api.createEvent(
                                dto
                            )
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