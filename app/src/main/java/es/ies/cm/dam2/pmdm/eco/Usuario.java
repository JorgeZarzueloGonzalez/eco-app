package es.ies.cm.dam2.pmdm.eco;

import java.io.Serializable;

/**
 * P12 Un usuario de Eco (tabla {@code usuarios} de {@code eco.db}).
 * <p>
 * {@code implements Serializable} permite pasarlo entre actividades dentro
 * de un Intent. La clave se guarda en texto plano porque es una práctica de
 * clase; en una app real iría cifrada.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String clave;

    public Usuario(String nombre, String clave) {
        this.id = 0;
        this.nombre = nombre;
        this.clave = clave;
    }

    public Usuario() {

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

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
