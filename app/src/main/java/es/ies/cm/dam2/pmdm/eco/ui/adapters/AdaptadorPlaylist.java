package es.ies.cm.dam2.pmdm.eco.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.model.Playlist;
import java.util.List;

/**
 * Proyecto: adaptador de la lista de playlists (tarjetas con nombre y número
 * de canciones). Clic corto: abrirla; clic largo: menú contextual.
 */
public class AdaptadorPlaylist extends RecyclerView.Adapter<AdaptadorPlaylist.Fila> {

    /** Lo que hace la activity con los clics. */
    public interface OyentePlaylist {
        void alAbrir(Playlist playlist);

        void alPulsarLargo(Playlist playlist, View tarjeta);
    }

    private final List<Playlist> playlists;
    private final OyentePlaylist oyente;

    public AdaptadorPlaylist(List<Playlist> playlists, OyentePlaylist oyente) {
        this.playlists = playlists;
        this.oyente = oyente;
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Fila(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Playlist p = playlists.get(posicion);
        fila.nombre.setText(p.getNombre());
        // Plural correcto en cada idioma ("1 canción" / "3 canciones")
        fila.total.setText(fila.itemView.getResources().getQuantityString(
                R.plurals.playlist_canciones, p.getNumCanciones(), p.getNumCanciones()));

        fila.itemView.setOnClickListener(v -> {
            int actual = fila.getBindingAdapterPosition();
            if (actual != RecyclerView.NO_POSITION) {
                oyente.alAbrir(playlists.get(actual));
            }
        });
        fila.itemView.setOnLongClickListener(v -> {
            int actual = fila.getBindingAdapterPosition();
            if (actual != RecyclerView.NO_POSITION) {
                oyente.alPulsarLargo(playlists.get(actual), v);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView total;

        Fila(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombrePlaylistFila);
            total = itemView.findViewById(R.id.totalPlaylistFila);
        }
    }
}
