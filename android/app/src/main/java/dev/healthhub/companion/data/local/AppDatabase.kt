package dev.healthhub.companion.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "pending_uploads")
data class PendingUploadEntity(
    @PrimaryKey val clientId: String,
    val kind: String,
    val payloadJson: String,
    val createdAtEpochMs: Long,
    val status: String = "PENDING"
)

@Dao
interface PendingUploadDao {

    @Query("SELECT * FROM pending_uploads WHERE status = 'PENDING' ORDER BY createdAtEpochMs LIMIT :limit")
    suspend fun pending(limit: Int): List<PendingUploadEntity>

    @Query("SELECT COUNT(*) FROM pending_uploads WHERE status = 'PENDING'")
    suspend fun pendingCount(): Int

    @Insert
    suspend fun insertAll(entities: List<PendingUploadEntity>)

    @Query("DELETE FROM pending_uploads WHERE clientId IN (:clientIds)")
    suspend fun deleteByClientIds(clientIds: List<String>)

    @Query("UPDATE pending_uploads SET status = 'FAILED' WHERE clientId IN (:clientIds)")
    suspend fun markFailed(clientIds: List<String>)
}

@Database(entities = [PendingUploadEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingUploadDao(): PendingUploadDao
}
