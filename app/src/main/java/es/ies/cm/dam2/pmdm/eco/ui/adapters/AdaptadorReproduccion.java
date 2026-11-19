package es.ies.cm.dam2.pmdm.eco.ui.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.model.Reproduccion;
import java.util.List;

/**
 * P13 Adaptador de la lista de reproducciones: oyente y puntuación en la
 * primera línea, fecha en la segunda. El clic largo avisa a la activity
 * para borrar la reproducción.
 */
public class AdaptadorReproduccion extends RecyclerView.Adapter<AdaptadorReproduccion.Fila> {

    /** Lo que hace la activity con el clic largo. */
    public interface OyenteBorrado {
        void alPedirBorrado(Reproduccion reproduccion);
    }

    private final List<Reproduccion> reproducciones;
    private final OyenteBorrado oyente;

    public AdaptadorReproduccion(List<Reproduccion> reproducciones, OyenteBorrado oyente) {
        this.reproducciones = reproducciones;
        this.oyente = oyente;
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reproduccion, parent, false);
        return new Fila(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Reproduccion r = reproducciones.get(posicion);
        fila.oyente.setText(fila.itemView.getContext().getString(
                R.string.reproduccion_fila, r.getOyente(), r.getPuntuacion()));
        // Fecha y hora con el formato del idioma del móvil.
        fila.fecha.setText(DateFormat.getMediumDateFormat(fila.itemView.getContext()).format(r.getFecha())
                + " · " + DateFormat.getTimeFormat(fila.itemView.getContext()).format(r.getFecha()));

        fila.itemView.setOnLongClickListener(v -> {
            int actual = fila.getBindingAdapterPosition();
            if (actual != RecyclerView.NO_POSITION) {
                oyente.alPedirBorrado(reproducciones.get(actual));
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return reproducciones.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView oyente;
        final TextView fecha;

        Fila(@NonNull View itemView) {
            super(itemView);
            oyente = itemView.findViewById(R.id.oyenteReproduccionFila);
            fecha = itemView.findViewById(R.id.fechaReproduccionFila);
        }
    }
}
