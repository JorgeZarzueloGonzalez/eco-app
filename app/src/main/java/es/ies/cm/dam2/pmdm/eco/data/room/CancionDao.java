package es.ies.cm.dam2.pmdm.eco.data.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import java.util.List;

/**
 * P13 Acceso a la tabla {@code canciones} de {@code eco_room.db}.
 * <p>
 * Room genera la implementación al compilar y comprueba el SQL de cada
 * {@code @Query}: si una columna está mal escrita, el proyecto no compila.
 * Ningún método se puede llamar en el hilo principal: van siempre dentro de
 * {@link AppDatabase#executor}.
 */
@Dao
public interface CancionDao {

    /** Alta: devuelve el {@code id} que genera Room. */
    @Insert
    long insertar(Cancion cancion);

    /**
     * Copia inicial desde SQLite: las canciones llegan con su {@code id} y,
     * si alguna ya estaba, se ignora en vez de fallar.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertarTodas(List<Cancion> canciones);

    /** Edición: actualiza la fila con el mismo {@code id}. Devuelve las filas tocadas. */
    @Update
    int actualizar(Cancion cancion);

    /** Borrado por {@code id}: dispara el CASCADE de sus reproducciones. */
    @Delete
    int borrar(Cancion cancion);

    @Query("SELECT * FROM canciones ORDER BY nombre COLLATE NOCASE")
    List<Cancion> todas();

    /** Una canción por su {@code id}, o {@code null} si se borró. */
    @Query("SELECT * FROM canciones WHERE id = :id")
    Cancion porId(int id);

    /**
     * Buscador: {@code patron} llega ya preparado desde Java ({@code texto%}
     * o {@code %texto%}); nunca se concatena dentro del SQL.
     */
    @Query("SELECT * FROM canciones "
            + "WHERE nombre LIKE :patron ESCAPE '\\' AND valoracion >= :valoracionMinima "
            + "ORDER BY nombre COLLATE NOCASE")
    List<Cancion> buscar(String patron, int valoracionMinima);

    /** Los géneros que hay en la biblioteca, sin repetir: alimentan el primer Spinner. */
    @Query("SELECT DISTINCT genero FROM canciones ORDER BY genero")
    List<String> generos();

    /** Las canciones de un género: el segundo Spinner, que depende del primero. */
    @Query("SELECT * FROM canciones WHERE genero = :genero ORDER BY nombre COLLATE NOCASE")
    List<Cancion> porGenero(String genero);

    @Query("SELECT COUNT(*) FROM canciones")
    int contar();
}
