package es.ies.cm.dam2.pmdm.eco.model;

/**
 * P12 Una fila de la tabla {@code usuarios} de {@code eco.db}.
 * <p>
 * Solo lleva lo que usa la app después del login: el {@code _id} (para
 * filtrar los favoritos) y el nombre (para el título). La clave se comprueba
 * en la consulta SQL y no hace falta guardarla en memoria.
 */
public class Usuario {

    private int id;
    private String nombre;

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
}
