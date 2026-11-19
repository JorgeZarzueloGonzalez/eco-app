package es.ies.cm.dam2.pmdm.eco.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * P8/P9 Los datos de una canción de la biblioteca.
 * <p>
 * {@code implements Serializable} es la etiqueta que permite meter el objeto
 * dentro de un Intent (extra). Sin ella salta {@code NotSerializableException}.
 * {@code toString()} sirve para poder pintar el objeto como texto legible.
 * <p>
 * P13: es una {@code @Entity} de Room (tabla {@code canciones} de
 * {@code eco_room.db}). Cada campo es una columna; Room los lee y escribe con
 * los getters/setters y crea los objetos con el constructor vacío (los otros
 * llevan {@code @Ignore} para que Room no dude cuál usar).
 */
@Entity(tableName = "canciones")
public class Cancion implements Serializable {

    /**
     * Identificador de la versión de la clase (lo pide Android Studio).
     */
    private static final long serialVersionUID = 2L;

    /** Valores de {@link #estado} (RadioGroup del alta). */
    public static final int ESTADO_NUEVA = 0;
    public static final int ESTADO_ESCUCHADA = 1;
    public static final int ESTADO_EN_BUCLE = 2;
    /** Clave primaria: si vale 0 al insertar, Room genera una nueva. */
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private String genero;
    private String sello;
    private int interpretes;
    private int anio;
    private int popularidad;
    private int duracion;

    /**
     * Valoración del teclado de puntuación de la P3 (de 1 a 9; 0 = sin valorar).
     */
    private int valoracion;

    /*
     * Proyecto (Room v2): tres columnas nuevas. defaultValue = "0" hace que el
     * esquema que espera Room coincida con el que crea la migración 1 -> 2
     * (ALTER TABLE ... DEFAULT 0); si no coinciden, Room lanza una excepción al abrir.
     */

    /** Estado de escucha: {@link #ESTADO_NUEVA}, {@link #ESTADO_ESCUCHADA} o {@link #ESTADO_EN_BUCLE}. */
    @ColumnInfo(defaultValue = "0")
    private int estado;

    /** Marcada como favorita con el Switch. */
    @ColumnInfo(defaultValue = "0")
    private boolean favorita;

    /** Contenido explícito (CheckBox). */
    @ColumnInfo(defaultValue = "0")
    private boolean explicita;

    @Ignore
    public Cancion(int id, String nombre, String genero, String sello,
                   int interpretes,
                   int anio, int popularidad, int duracion, int valoracion) {
        this.id = id;
        this.nombre = nombre;
        this.genero = genero;
        this.sello = sello;
        this.interpretes = interpretes;
        this.anio = anio;
        this.popularidad = popularidad;
        this.duracion = duracion;
        this.valoracion = valoracion;
    }

    @Ignore
    public Cancion(String nombre, String genero, String sello,
                   int interpretes,
                   int anio, int popularidad, int duracion, int valoracion) {
        this(0, nombre, genero, sello, interpretes, anio, popularidad, duracion, valoracion);
    }

    /** Constructor que usa Room para crear los objetos al leer filas. */
    public Cancion() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getSello() {
        return sello;
    }

    public void setSello(String sello) {
        this.sello = sello;
    }

    public int getInterpretes() {
        return interpretes;
    }

    public void setInterpretes(int interpretes) {
        this.interpretes = interpretes;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getPopularidad() {
        return popularidad;
    }

    public void setPopularidad(int popularidad) {
        this.popularidad = popularidad;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public int getValoracion() {
        return valoracion;
    }

    public void setValoracion(int valoracion) {
        this.valoracion = valoracion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public boolean isFavorita() {
        return favorita;
    }

    public void setFavorita(boolean favorita) {
        this.favorita = favorita;
    }

    public boolean isExplicita() {
        return explicita;
    }

    public void setExplicita(boolean explicita) {
        this.explicita = explicita;
    }

    @Override
    public String toString() {
        return nombre + " · " + genero + " · Sello: " + sello
                + " · Intérpretes: " + interpretes
                + " · Año: " + anio
                + " · Popularidad: " + popularidad
                + " · Valoración: " + valoracion
                + " · Duración: " + duracion + " s"
                + " · Estado: " + estado + " · Favorita: " + favorita;
    }
}
