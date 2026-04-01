package com.firsov.substation.data.local

import androidx.room.*
import com.firsov.substation.data.model.Port
import kotlinx.coroutines.flow.Flow

@Dao
interface PortDao {

    @Query("SELECT * FROM port")
    fun getAll(): Flow<List<Port>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ports: List<Port>)

    @Update
    suspend fun update(port: Port)

    @Delete
    suspend fun delete(port: Port)

    @Query("DELETE FROM port WHERE containerId = :containerId")
    suspend fun deleteByContainer(containerId: String)
}