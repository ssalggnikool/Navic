package paige.navic.data.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Clock
import kotlin.time.Instant

enum class SyncActionType {
	STAR, UNSTAR, DELETE_PLAYLIST, SCROBBLE,

	// this is dumb but it works so whatever
	STAR_0, STAR_1, STAR_2, STAR_3, STAR_4, STAR_5
}

@Entity
data class SyncActionEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val actionType: SyncActionType,
	val itemId: String,
	@ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
	val time: Instant = Clock.System.now()
)
