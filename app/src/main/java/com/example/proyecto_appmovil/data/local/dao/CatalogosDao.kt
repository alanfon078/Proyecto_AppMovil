package com.example.proyecto_appmovil.data.local.dao

import androidx.room.*
import com.example.proyecto_appmovil.data.local.entity.CarreraEntity
import com.example.proyecto_appmovil.data.local.entity.EstadoEntity
import com.example.proyecto_appmovil.data.local.entity.MunicipioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarreras(carreras: List<CarreraEntity>)

    @Query("SELECT * FROM carreras WHERE activa = 1 ORDER BY nombre ASC")
    fun getCarrerasFlow(): Flow<List<CarreraEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstados(estados: List<EstadoEntity>)

    @Query("SELECT * FROM estados ORDER BY nombre ASC")
    fun getEstadosFlow(): Flow<List<EstadoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMunicipios(municipios: List<MunicipioEntity>)

    @Query("SELECT * FROM municipios WHERE idEstado = :idEstado ORDER BY nombre ASC")
    fun getMunicipiosPorEstadoFlow(idEstado: Int): Flow<List<MunicipioEntity>>

    @Query("DELETE FROM carreras")
    suspend fun clearCarreras()

    @Query("DELETE FROM estados")
    suspend fun clearEstados()

    @Query("DELETE FROM municipios")
    suspend fun clearMunicipios()
}
