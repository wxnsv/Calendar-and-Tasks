package com.nikkap.calendar.data.local.entity

interface SyncableEntity {
    val id: String
    val lastModified: Long
    val pendingAction: PendingActions
}
