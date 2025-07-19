package com.app.cicdmonitor.data.database.converters

import androidx.room.TypeConverter
import com.app.cicdmonitor.data.models.*

class Converters {
    
    @TypeConverter
    fun fromCiProvider(provider: CiProvider): String = provider.name
    
    @TypeConverter
    fun toCiProvider(provider: String): CiProvider = CiProvider.valueOf(provider)
    
    @TypeConverter
    fun fromPipelineStatus(status: PipelineStatus): String = status.name
    
    @TypeConverter
    fun toPipelineStatus(status: String): PipelineStatus = PipelineStatus.valueOf(status)
    
    @TypeConverter
    fun fromBuildStatus(status: BuildStatus): String = status.name
    
    @TypeConverter
    fun toBuildStatus(status: String): BuildStatus = BuildStatus.valueOf(status)
    
    @TypeConverter
    fun fromTokenType(type: TokenType): String = type.name
    
    @TypeConverter
    fun toTokenType(type: String): TokenType = TokenType.valueOf(type)
}
