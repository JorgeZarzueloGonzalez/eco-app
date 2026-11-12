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

    /**
     * Identificador de la versión de la clase (lo pide Android Studio).
     */
    private static final long serialVersionUID = 1L;
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


    public Cancion(String nombre, String genero, String sello,
                   int interpretes,
                   int anio, int popularidad, int duracion, int valoracion) {
        this.id = 0;
        this.nombre = nombre;
        this.genero = genero;
        this.sello = sello;
        this.interpretes = interpretes;
        this.anio = anio;
        this.popularidad = popularidad;
        this.duracion = duracion;
        this.valoracion = valoracion;
    }

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

    /**
     * Texto de intérpretes para las filas de la lista.
     */

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
