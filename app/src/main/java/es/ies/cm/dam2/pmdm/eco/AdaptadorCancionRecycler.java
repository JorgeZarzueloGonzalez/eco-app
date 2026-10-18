package es.ies.cm.dam2.pmdm.eco;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * P9 Adaptador del {@link RecyclerView}: pinta cada canción en una tarjeta
 * (portada + nombre + intérpretes) y avisa de los clics cortos y largos.
 * <p>
 * Lleva dentro su {@link CancionViewHolder}, la "caché" de las vistas de una fila.
 */
public class AdaptadorCancionRecycler
        extends RecyclerView.Adapter<AdaptadorCancionRecycler.CancionViewHolder> {

    /** Quien quiera enterarse de los clics tiene que implementar esto. */
    public interface OyenteCancion {
        void alHacerClicCorto(int posicion);

        void alHacerClicLargo(int posicion);
    }

    private final List<Cancion> canciones;
    private final OyenteCancion oyente;

    public AdaptadorCancionRecycler(List<Cancion> canciones, OyenteCancion oyente) {
        this.canciones = canciones;
        this.oyente = oyente;
    }

    /** La caché de las vistas de una fila: evita repetir findViewById al hacer scroll. */
    static class CancionViewHolder extends RecyclerView.ViewHolder {

        final ImageView portada;
        final TextView nombre;
        final TextView interpretes;

        CancionViewHolder(@NonNull View fila) {
            super(fila);
            portada = fila.findViewById(R.id.portadaCancion);
            nombre = fila.findViewById(R.id.nombreCancionFila);
            interpretes = fila.findViewById(R.id.interpretesCancionFila);
        }
    }

    @NonNull
    @Override
    public CancionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View fila = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cancion, parent, false);
        return new CancionViewHolder(fila);
    }

    @Override
    public void onBindViewHolder(@NonNull CancionViewHolder holder, int position) {
        Cancion cancion = canciones.get(position);

        holder.portada.setImageResource(Portadas.de(cancion.getGenero()));
        holder.nombre.setText(cancion.getNombre());
        holder.interpretes.setText(holder.itemView.getContext()
                .getString(R.string.item_interpretes, cancion.getInterpretesTexto()));

        // Los dos clics van en la tarjeta (la vista de la fila), no en el RecyclerView.
        holder.itemView.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                oyente.alHacerClicCorto(posicion);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                oyente.alHacerClicLargo(posicion);
            }
            // true = el evento lo hemos consumido: NO se abre también la ficha.
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return canciones.size();
    }
}
