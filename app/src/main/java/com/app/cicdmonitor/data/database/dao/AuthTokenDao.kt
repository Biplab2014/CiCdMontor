package com.app.cicdmonitor.data.database.dao

import androidx.room.*
import com.app.cicdmonitor.data.models.AuthToken
import com.app.cicdmonitor.data.models.CiProvider
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthTokenDao {
    
    @Query("SELECT * FROM auth_tokens WHERE isActive = 1")
    fun getAllActiveTokens(): Flow<List<AuthToken>>
    
    @Query("SELECT * FROM auth_tokens WHERE provider = :provider AND isActive = 1 LIMIT 1")
    suspend fun getTokenByProvider(provider: CiProvider): AuthToken?
    
    @Query("SELECT * FROM auth_tokens WHERE id = :id")
    suspend fun getTokenById(id: String): AuthToken?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: AuthToken)
    
    @Update
    suspend fun updateToken(token: AuthToken)
    
    @Query("UPDATE auth_tokens SET isActive = 0 WHERE provider = :provider")
    suspend fun deactivateTokensByProvider(provider: CiProvider)
    
    @Query("DELETE FROM auth_tokens WHERE id = :id")
    suspend fun deleteToken(id: String)
    
    @Query("DELETE FROM auth_tokens WHERE provider = :provider")
    suspend fun deleteTokensByProvider(provider: CiProvider)
    
    @Query("SELECT COUNT(*) FROM auth_tokens WHERE isActive = 1")
    suspend fun getActiveTokenCount(): Int
}
