package es.ies.cm.dam2.pmdm.eco.data.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import es.ies.cm.dam2.pmdm.eco.model.Reproduccion;
import java.util.List;

/**
 * P13 Acceso a la tabla {@code reproducciones} de {@code eco_room.db}
 * (las "partidas" del enunciado).
 */
@Dao
public interface ReproduccionDao {

    @Insert
    long insertar(Reproduccion reproduccion);

    @Update
    int actualizar(Reproduccion reproduccion);

    @Delete
    int borrar(Reproduccion reproduccion);

    /** Las reproducciones de una canción, de la más reciente a la más antigua. */
    @Query("SELECT * FROM reproducciones WHERE cancionId = :cancionId ORDER BY fecha DESC")
    List<Reproduccion> porCancion(int cancionId);

    @Query("SELECT * FROM reproducciones WHERE id = :id")
    Reproduccion porId(int id);

    /** Total de reproducciones de una canción. */
    @Query("SELECT COUNT(*) FROM reproducciones WHERE cancionId = :cancionId")
    int contar(int cancionId);
}
