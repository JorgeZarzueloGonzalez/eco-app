package es.ies.cm.dam2.pmdm.eco.model;

/**
 * Proyecto: una playlist de un usuario (tabla {@code playlists} de {@code eco.db}).
 * <p>
 * Es la "lista personal" del enunciado: cada usuario tiene las suyas, con el
 * nombre que quiera. Las canciones de cada playlist están en la tabla
 * intermedia {@code playlist_canciones}.
 */
public class Playlist {

    private int id;
    private String nombre;

    /** Cuántas canciones tiene (no es una columna: lo calcula la consulta con COUNT). */
    private int numCanciones;

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

    public int getNumCanciones() {
        return numCanciones;
    }

    public void setNumCanciones(int numCanciones) {
        this.numCanciones = numCanciones;
    }
}
