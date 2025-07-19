package com.app.cicdmonitor.data.database.dao

import androidx.room.*
import com.app.cicdmonitor.data.models.User
import com.app.cicdmonitor.data.models.CiProvider
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE isActive = 1")
    fun getAllActiveUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE provider = :provider AND isActive = 1 LIMIT 1")
    suspend fun getUserByProvider(provider: CiProvider): User?
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("UPDATE users SET isActive = 0 WHERE provider = :provider")
    suspend fun deactivateUsersByProvider(provider: CiProvider)
    
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
    
    @Query("DELETE FROM users WHERE provider = :provider")
    suspend fun deleteUsersByProvider(provider: CiProvider)
}
