package es.ies.cm.dam2.pmdm.eco;

import java.io.Serializable;

/**
 * P8 Los datos de una canción de la biblioteca.
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
    private final int interpretesMin;
    private final int interpretesMax;
    private final int anio;
    private final int popularidad;
    private final int duracion;

    public Cancion(String nombre, String genero, String sello,
                   int interpretesMin, int interpretesMax,
                   int anio, int popularidad, int duracion) {
        this.nombre = nombre;
        this.genero = genero;
        this.sello = sello;
        this.interpretesMin = interpretesMin;
        this.interpretesMax = interpretesMax;
        this.anio = anio;
        this.popularidad = popularidad;
        this.duracion = duracion;
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

    public int getInterpretesMin() {
        return interpretesMin;
    }

    public int getInterpretesMax() {
        return interpretesMax;
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

    /** Texto de intérpretes para las filas de la lista. */
    public String getInterpretesTexto() {
        return interpretesMin + " - " + interpretesMax;
    }

    @Override
    public String toString() {
        return nombre + " · " + genero + " · Sello: " + sello
                + " · Intérpretes: " + getInterpretesTexto()
                + " · Año: " + anio
                + " · Popularidad: " + popularidad
                + " · Duración: " + duracion + " min";
    }
}
