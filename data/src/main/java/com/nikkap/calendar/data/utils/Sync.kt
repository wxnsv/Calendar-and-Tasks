package com.nikkap.calendar.data.utils

import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.SyncableEntity
import com.nikkap.calendar.data.local.entity.TaskListEntity

suspend fun <T : SyncableEntity> localSyncEntities(
    remoteEntities: List<T>,
    getLocalEntities: suspend () -> List<T>,
    deleteEntitiesByIds: suspend (List<String>) -> Unit,
    insertEntities: suspend (List<T>) -> Unit
) {

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
}

suspend fun localSyncTaskLists(
    remoteLists: List<TaskListEntity>,
    getLocalLists: suspend () -> List<TaskListEntity>,
    deleteListsByIds: suspend (List<String>) -> Unit,
    insertLists: suspend (List<TaskListEntity>) -> Unit,
    deleteListAndTasks: suspend (String) -> Unit,
) {

    val localEntities = getLocalLists()
    val localMap = localEntities.associateBy { it.id }

    val (itemsToDelete, nonDeleteItems) = remoteLists.partition {
        it.pendingAction == PendingActions.DELETE
    }

    if (itemsToDelete.isNotEmpty()) {
        deleteListsByIds(itemsToDelete.map { it.id })
    }


    val remoteIds = remoteLists.map { it.id }.toSet()
    localEntities.filter { it.pendingAction == PendingActions.NONE }.forEach { localList ->
        if (!remoteIds.contains(localList.id)) deleteListAndTasks(localList.id)
    }


    val insertItems = nonDeleteItems.filter { remote ->
        val local = localMap[remote.id]
        local == null || remote.lastModified > local.lastModified
    }

    if (insertItems.isNotEmpty()) {
        insertLists(insertItems)
    }
}