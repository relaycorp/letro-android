package tech.relaycorp.letro.notification.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.notification.storage.entity.Notification
import tech.relaycorp.letro.notification.storage.entity.TABLE_NAME_NOTIFICATIONS

@Dao
interface NotificationsDao {

    @Query("SELECT * FROM $TABLE_NAME_NOTIFICATIONS")
    fun getAll(): Flow<List<Notification>>

    @Insert
    fun insert(notification: Notification)

    @Update
    fun update(notification: Notification)
}
