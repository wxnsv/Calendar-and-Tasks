package com.nikkap.calendar.data.utils

import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.SyncableEntity

suspend fun <T : SyncableEntity> syncEntities(
    remoteEntities: List<T>,
    getLocalEntities: suspend () -> List<T>,
    deleteEntitiesByIds: suspend (List<String>) -> Unit,
    insertEntities: suspend (List<T>) -> Unit
): Result<Unit> {

    val localEntities = getLocalEntities()
    val localMap = localEntities.associateBy { it.id }

    val (itemsToDelete, nonDeleteItems) = remoteEntities.partition {
        it.pendingAction == PendingActions.DELETE
    }

    if (itemsToDelete.isNotEmpty()) {
        deleteEntitiesByIds(itemsToDelete.map { it.id })
    }

    val insertItems = nonDeleteItems.filter { remote ->
        val local = localMap[remote.id]
        local == null || remote.lastModified > local.lastModified
    }

    if (insertItems.isNotEmpty()) {
        insertEntities(insertItems)
    }

    return Result.success(Unit)
}