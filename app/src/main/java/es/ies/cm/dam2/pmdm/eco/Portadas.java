package es.ies.cm.dam2.pmdm.eco;

/**
 * Las portadas de Eco: recursos locales en {@code res/drawable} (sin permisos ni
 * librerías de internet). Se elige una según el género de la canción.
 */
public final class Portadas {

    private Portadas() {
        // Clase de utilidades: no se instancia
    }

    public static int de(String genero) {
        if (genero == null) {
            return R.drawable.portada_eco;
        }
        switch (genero) {
            case "Rock":
                return R.drawable.portada_rock;
            case "Pop":
                return R.drawable.portada_pop;
            case "Electrónica":
                return R.drawable.portada_electronica;
            default:
                return R.drawable.portada_eco;
        }
    }
}
