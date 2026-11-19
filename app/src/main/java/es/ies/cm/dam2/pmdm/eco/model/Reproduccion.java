package es.ies.cm.dam2.pmdm.eco.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * P13 Una reproducción (sesión de escucha) de una canción: la "partida" del enunciado.
 * <p>
 * Relación 1-N: una {@link Cancion} tiene muchas reproducciones y cada
 * reproducción pertenece a una sola canción. Por eso la clave ajena va aquí
 * ({@code cancionId}) y no en {@code Cancion}.
 * <ul>
 *     <li>{@code onDelete = CASCADE}: al borrar la canción, Room borra sus reproducciones.</li>
 *     <li>{@code @Index("cancionId")}: sin él, cada borrado recorre toda la tabla
 *     (y Room avisa al compilar).</li>
 * </ul>
 */
@Entity(tableName = "reproducciones",
        foreignKeys = @ForeignKey(
                entity = Cancion.class,
                parentColumns = "id",
                childColumns = "cancionId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("cancionId"))
public class Reproduccion {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /** El {@code id} de la canción reproducida (clave ajena). */
    private int cancionId;

    /** Momento de la escucha en milisegundos ({@code System.currentTimeMillis()}). */
    private long fecha;

    /** Puntuación de la escucha, de 1 a 9 (como la valoración de la app). */
    private int puntuacion;

    /** Quién escuchó la canción (el "ganador" del enunciado). */
    private String oyente;

    public Reproduccion() {
    }

    @Ignore
    public Reproduccion(int cancionId, long fecha, int puntuacion, String oyente) {
        this.cancionId = cancionId;
        this.fecha = fecha;
        this.puntuacion = puntuacion;
        this.oyente = oyente;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCancionId() {
        return cancionId;
    }

    public void setCancionId(int cancionId) {
        this.cancionId = cancionId;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getOyente() {
        return oyente;
    }

    public void setOyente(String oyente) {
        this.oyente = oyente;
    }
}
