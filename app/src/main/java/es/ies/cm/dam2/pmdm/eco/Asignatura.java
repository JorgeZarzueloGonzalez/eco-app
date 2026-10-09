package es.ies.cm.dam2.pmdm.eco;

public class Asignatura {

    private String nombre;
    private String curso;
    private String instituto;

    public Asignatura(String nombre, String curso, String instituto) {
        this.nombre = nombre;
        this.curso = curso;
        this.instituto = instituto;
    }

    public String getInstituto() {
        return instituto;
    }

    public void setInstituto(String instituto) {
        this.instituto = instituto;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
