package es.ies.cm.dam2.pmdm.eco.util;

import android.content.Context;
import android.text.format.DateFormat;
import androidx.annotation.ColorRes;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import java.util.Locale;

/**
 * Cómo se enseñan al usuario algunos datos guardados como números.
 * Centralizarlo aquí evita repetir el mismo código en varias pantallas.
 */
public final class Formatos {

    private Formatos() {
        // Clase de utilidades: no se instancia
    }

    /** Texto del estado de escucha, en el idioma del móvil. */
    public static String estado(Context context, int estado) {
        if (estado == Cancion.ESTADO_ESCUCHADA) return context.getString(R.string.state_listened);
        if (estado == Cancion.ESTADO_EN_BUCLE) return context.getString(R.string.state_repeat);
        return context.getString(R.string.state_new);
    }

    /** Color de la etiqueta del estado. */
    @ColorRes
    public static int colorEstado(int estado) {
        if (estado == Cancion.ESTADO_ESCUCHADA) return R.color.estado_escuchado;
        if (estado == Cancion.ESTADO_EN_BUCLE) return R.color.estado_favorito;
        return R.color.estado_nuevo;
    }

    /** Segundos a "m:ss" (205 -> "3:25"). */
    public static String duracion(int segundos) {
        return String.format(Locale.ROOT, "%d:%02d", segundos / 60, segundos % 60);
    }

    /** Fecha y hora con el formato del idioma del móvil ("19 nov 2026 · 18:55"). */
    public static String fechaHora(Context context, long milis) {
        return context.getString(R.string.fecha_hora,
                DateFormat.getMediumDateFormat(context).format(milis),
                DateFormat.getTimeFormat(context).format(milis));
    }
}
