package com.firsov.substation.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.firsov.substation.data.model.Cell
import kotlinx.coroutines.flow.Flow

@Dao
interface CellDao {
    @Query("SELECT * FROM cells")
    fun getAllCells(): Flow<List<Cell>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(cell: Cell)

    @Delete
    suspend fun delete(cell: Cell)
}