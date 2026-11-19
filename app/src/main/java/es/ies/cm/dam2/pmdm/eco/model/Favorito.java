package es.ies.cm.dam2.pmdm.eco.model;

/**
 * P12 Una fila de la tabla {@code favoritos} de {@code eco.db}.
 * <p>
 * Solo guarda identificadores: el nombre y el género de la canción se piden
 * a Room con {@code cancionDao().porId(cancionId)} (P13: no hay JOIN posible
 * entre dos bases de datos). El usuario no hace falta: la consulta ya filtra
 * por él.
 */
public class Favorito {

    private int id;
    private int cancionId;

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
}
