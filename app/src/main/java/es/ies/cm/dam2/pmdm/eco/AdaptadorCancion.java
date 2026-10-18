package es.ies.cm.dam2.pmdm.eco;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * P8 Adaptador propio: sabe pintar una fila de canción (portada + nombre +
 * intérpretes) y alterna el color de fondo según la posición.
 */
public class AdaptadorCancion extends ArrayAdapter<Cancion> {

    private final Context contexto;
    private final List<Cancion> canciones;

    public AdaptadorCancion(Context contexto, List<Cancion> canciones) {
        super(contexto, R.layout.item_cancion, canciones);
        this.contexto = contexto;
        this.canciones = canciones;
    }

    @NonNull
    @Override
    public View getView(int position, View filaReciclada, @NonNull ViewGroup parent) {
        // Si Android no tiene una fila para reciclar, se infla el XML del ítem.
        View fila = filaReciclada;
        if (fila == null) {
            fila = LayoutInflater.from(contexto).inflate(R.layout.item_cancion, parent, false);
        }

        Cancion cancion = canciones.get(position);

        ImageView portada = fila.findViewById(R.id.portadaCancion);
        TextView nombre = fila.findViewById(R.id.nombreCancionFila);
        TextView interpretes = fila.findViewById(R.id.interpretesCancionFila);

        portada.setImageResource(portadaDe(cancion.getGenero()));
        nombre.setText(cancion.getNombre());
        interpretes.setText(contexto.getString(R.string.item_interpretes, cancion.getInterpretesTexto()));

        // Tono alterno: las vistas se reciclan, así que hay que repintar SIEMPRE el fondo.
        int color = position % 2 == 0 ? R.color.primary : R.color.primaryDark;
        fila.setBackgroundColor(ContextCompat.getColor(contexto, color));

        return fila;
    }

    /** Portada según el género de la canción (recursos locales, sin permisos). */
    private int portadaDe(String genero) {
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
