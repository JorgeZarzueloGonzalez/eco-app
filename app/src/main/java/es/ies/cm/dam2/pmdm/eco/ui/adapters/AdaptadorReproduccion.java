package es.ies.cm.dam2.pmdm.eco.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.model.Reproduccion;
import es.ies.cm.dam2.pmdm.eco.util.Formatos;
import java.util.List;

/**
 * P13 Adaptador de la lista de reproducciones: oyente y puntuación en la
 * primera línea, fecha en la segunda. Clic corto: editar; clic largo: borrar
 * (los dos los resuelve la activity).
 */
public class AdaptadorReproduccion extends RecyclerView.Adapter<AdaptadorReproduccion.Fila> {

    /** Lo que hace la activity con los clics. */
    public interface OyenteReproduccion {
        void alPulsar(Reproduccion reproduccion);

        void alPedirBorrado(Reproduccion reproduccion);
    }

    private final List<Reproduccion> reproducciones;
    private final OyenteReproduccion oyente;

    public AdaptadorReproduccion(List<Reproduccion> reproducciones, OyenteReproduccion oyente) {
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
        fila.fecha.setText(Formatos.fechaHora(fila.itemView.getContext(), r.getFecha()));

        fila.itemView.setOnClickListener(v -> {
            int actual = fila.getBindingAdapterPosition();
            if (actual != RecyclerView.NO_POSITION) {
                oyente.alPulsar(reproducciones.get(actual));
            }
        });

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
