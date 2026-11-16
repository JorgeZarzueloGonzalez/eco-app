package es.ies.cm.dam2.pmdm.eco.model;

import java.io.Serializable;

/**
 * P12 Una fila de la tabla {@code favoritos} de {@code eco.db}: relaciona un
 * usuario con una canción.
 * <p>
 * Aquí solo se guardan los dos identificadores; el nombre y el género de la
 * canción se leen de la tabla {@code canciones} por su {@code _id}.
 */
public class Favorito implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int usuarioId;
    private int cancionId;

    public Favorito(int usuarioId, int cancionId) {
        this.id = 0;
        this.usuarioId = usuarioId;
        this.cancionId = cancionId;
    }

    public Favorito() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getCancionId() {
        return cancionId;
    }

    public void setCancionId(int cancionId) {
        this.cancionId = cancionId;
    }

    @Override
    public String toString() {
        return "Favorito · usuario " + usuarioId + " · canción " + cancionId;
    }
}
