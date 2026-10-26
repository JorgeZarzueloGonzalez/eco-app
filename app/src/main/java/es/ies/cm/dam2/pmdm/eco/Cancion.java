package es.ies.cm.dam2.pmdm.eco;

import java.io.Serializable;

/**
 * P8/P9 Los datos de una canción de la biblioteca.
 * <p>
 * {@code implements Serializable} es la etiqueta que permite meter el objeto
 * dentro de un Intent (extra). Sin ella salta {@code NotSerializableException}.
 * {@code toString()} sirve para poder pintar el objeto como texto legible.
 */
public class Cancion implements Serializable {

    /** Identificador de la versión de la clase (lo pide Android Studio). */
    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final String genero;
    private final String sello;
    private final int interpretes;
    private final int anio;
    private final int popularidad;
    private final int duracion;

    /** Valoración del teclado de puntuación de la P3 (de 1 a 9; 0 = sin valorar). */
    private final int valoracion;

    public Cancion(String nombre, String genero, String sello,
                   int interpretes,
                   int anio, int popularidad, int duracion, int valoracion) {
        this.nombre = nombre;
        this.genero = genero;
        this.sello = sello;
        this.interpretes = interpretes;
        this.anio = anio;
        this.popularidad = popularidad;
        this.duracion = duracion;
        this.valoracion = valoracion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGenero() {
        return genero;
    }

    public String getSello() {
        return sello;
    }

    public int getInterpretes() {
        return interpretes;
    }

    public int getAnio() {
        return anio;
    }

    public int getPopularidad() {
        return popularidad;
    }

    public int getDuracion() {
        return duracion;
    }

    public int getValoracion() {
        return valoracion;
    }

    /** Texto de intérpretes para las filas de la lista. */

    @Override
    public String toString() {
        return nombre + " · " + genero + " · Sello: " + sello
                + " · Intérpretes: " + interpretes
                + " · Año: " + anio
                + " · Popularidad: " + popularidad
                + " · Valoración: " + valoracion
                + " · Duración: " + duracion + " min";
    }
}
