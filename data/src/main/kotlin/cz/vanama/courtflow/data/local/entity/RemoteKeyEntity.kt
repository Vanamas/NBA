package cz.vanama.courtflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table holding the `next_cursor` of the last fetched page of the
 * unfiltered player list. The balldontlie cursor is forward-only, so one
 * cursor is all the [androidx.paging.RemoteMediator] needs; a `null`
 * [nextCursor] means the API reported the end of the list.
 */
@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val nextCursor: Int?,
) {
    companion object {
        /** The unfiltered player list has exactly one cursor row. */
        const val SINGLETON_ID = 0
    }
}
