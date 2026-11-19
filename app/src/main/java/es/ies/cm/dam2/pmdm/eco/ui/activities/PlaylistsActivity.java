package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.data.room.AppDatabase;
import es.ies.cm.dam2.pmdm.eco.data.sqlite.EcoDB;
import es.ies.cm.dam2.pmdm.eco.model.Playlist;
import es.ies.cm.dam2.pmdm.eco.model.Usuario;
import es.ies.cm.dam2.pmdm.eco.ui.adapters.AdaptadorPlaylist;
import es.ies.cm.dam2.pmdm.eco.ui.dialogs.ConfirmarDialogFragment;
import es.ies.cm.dam2.pmdm.eco.ui.dialogs.TextoDialogFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * Proyecto: las playlists del usuario que ha iniciado sesión (la "lista
 * personal" del enunciado, distinta para cada usuario).
 * <ul>
 *     <li>Recibe el {@code _id} del usuario del login ({@link #EXTRA_USUARIO_ID}).</li>
 *     <li>Clic corto: abre la playlist. Clic largo: Renombrar / Eliminar.</li>
 *     <li>Menú de opciones: Nueva playlist y la cuenta (cambiar contraseña,
 *     cerrar sesión y eliminar cuenta).</li>
 * </ul>
 */
public class PlaylistsActivity extends AppCompatActivity
        implements AdaptadorPlaylist.OyentePlaylist {

    /** Extra con el {@code _id} del usuario que ha hecho login. */
    public static final String EXTRA_USUARIO_ID = "extra_usuario_id";

    // Claves de las respuestas de los diálogos
    private static final String NUEVA = "playlist_nueva";
    private static final String RENOMBRAR = "playlist_renombrar";
    private static final String BORRAR = "playlist_borrar";
    private static final String CLAVE = "cuenta_clave";
    private static final String BORRAR_CUENTA = "cuenta_borrar";

    private final ArrayList<Playlist> playlists = new ArrayList<>();

    private EcoDB ecoDB;
    private AdaptadorPlaylist adaptador;
    private TextView vacio;
    private MaterialToolbar toolbar;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playlists);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        usuarioId = getIntent().getIntExtra(EXTRA_USUARIO_ID, 0);
        if (usuarioId <= 0) {
            // Sin usuario no hay lista personal: se vuelve atrás
            finish();
            return;
        }
        ecoDB = EcoDB.getInstance(this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.main_playlists);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        vacio = findViewById(R.id.playlistsVacio);
        RecyclerView recycler = findViewById(R.id.playlistsRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorPlaylist(playlists, this);
        recycler.setAdapter(adaptador);

        escucharDialogos();

        // Título con el nombre del usuario
        AppDatabase.executor.execute(() -> {
            Usuario usuario = ecoDB.getUsuario(usuarioId);
            runOnUiThread(() -> {
                if (usuario == null) {   // la cuenta ya no existe
                    finish();
                } else {
                    toolbar.setTitle(getString(R.string.playlists_titulo, usuario.getNombre()));
                }
            });
        });
    }

    /** Al volver de una playlist se recarga (puede haber cambiado su número de canciones). */
    @Override
    protected void onResume() {
        super.onResume();
        if (adaptador != null) {
            cargar();
        }
    }

    private void cargar() {
        AppDatabase.executor.execute(() -> {
            List<Playlist> leidas = ecoDB.getPlaylists(usuarioId);
            runOnUiThread(() -> {
                playlists.clear();
                playlists.addAll(leidas);
                adaptador.notifyDataSetChanged();
                vacio.setVisibility(playlists.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    // ===================== clics en las tarjetas =====================

    @Override
    public void alAbrir(Playlist playlist) {
        Intent intent = new Intent(this, PlaylistActivity.class);
        intent.putExtra(PlaylistActivity.EXTRA_PLAYLIST_ID, playlist.getId());
        startActivity(intent);
    }

    /** Menú contextual de la tarjeta. */
    @Override
    public void alPulsarLargo(Playlist playlist, View tarjeta) {
        PopupMenu menu = new PopupMenu(this, tarjeta);
        menu.getMenuInflater().inflate(R.menu.menu_contextual_playlist, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.playlistRenombrar) {
                TextoDialogFragment.pedirTexto(getSupportFragmentManager(), RENOMBRAR,
                        getString(R.string.playlist_renombrar), getString(R.string.playlist_nombre),
                        playlist.getNombre(), playlist.getId());
                return true;
            }
            if (item.getItemId() == R.id.playlistEliminar) {
                ConfirmarDialogFragment.mostrar(getSupportFragmentManager(), BORRAR,
                        getString(R.string.playlist_eliminar_titulo),
                        getString(R.string.playlist_eliminar_pregunta, playlist.getNombre()),
                        playlist.getId());
                return true;
            }
            return false;
        });
        menu.show();
    }

    // ===================== menú de opciones =====================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlists, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.playlistNueva) {
            TextoDialogFragment.pedirTexto(getSupportFragmentManager(), NUEVA,
                    getString(R.string.playlist_nueva), getString(R.string.playlist_nombre), "", 0);
            return true;
        }
        if (id == R.id.cuentaClave) {
            TextoDialogFragment.pedirClaves(getSupportFragmentManager(), CLAVE,
                    getString(R.string.cuenta_cambiar_clave), getString(R.string.cuenta_clave_actual),
                    getString(R.string.cuenta_clave_nueva));
            return true;
        }
        if (id == R.id.cuentaSalir) {
            finish();
            return true;
        }
        if (id == R.id.cuentaBorrar) {
            ConfirmarDialogFragment.mostrar(getSupportFragmentManager(), BORRAR_CUENTA,
                    getString(R.string.cuenta_eliminar), getString(R.string.cuenta_eliminar_pregunta),
                    usuarioId);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ===================== respuestas de los diálogos =====================

    /**
     * Las respuestas llegan por la Fragment Result API. Se registran en onCreate
     * para que sigan funcionando si se gira el móvil con un diálogo abierto.
     */
    private void escucharDialogos() {
        getSupportFragmentManager().setFragmentResultListener(NUEVA, this, (k, r) -> {
            String nombre = r.getString(TextoDialogFragment.RESULTADO_TEXTO);
            AppDatabase.executor.execute(() -> {
                long nueva = ecoDB.crearPlaylist(usuarioId, nombre);
                aviso(nueva == -1 ? getString(R.string.playlist_repetida)
                        : getString(R.string.playlist_creada, nombre));
            });
            cargar();
        });

        getSupportFragmentManager().setFragmentResultListener(RENOMBRAR, this, (k, r) -> {
            String nombre = r.getString(TextoDialogFragment.RESULTADO_TEXTO);
            int playlistId = r.getInt(TextoDialogFragment.RESULTADO_ID);
            AppDatabase.executor.execute(() -> {
                if (!ecoDB.renombrarPlaylist(playlistId, nombre)) {
                    aviso(getString(R.string.playlist_repetida));
                }
            });
            cargar();
        });

        getSupportFragmentManager().setFragmentResultListener(BORRAR, this, (k, r) -> {
            int playlistId = r.getInt(ConfirmarDialogFragment.RESULTADO_ID);
            AppDatabase.executor.execute(() -> {
                ecoDB.borrarPlaylist(playlistId);   // sus canciones se van en cascada
                aviso(getString(R.string.playlist_eliminada));
            });
            cargar();
        });

        getSupportFragmentManager().setFragmentResultListener(CLAVE, this, (k, r) -> {
            String actual = r.getString(TextoDialogFragment.RESULTADO_TEXTO);
            String nueva = r.getString(TextoDialogFragment.RESULTADO_TEXTO2);
            AppDatabase.executor.execute(() -> {
                boolean cambiada = ecoDB.cambiarClave(usuarioId, actual, nueva);
                aviso(getString(cambiada ? R.string.cuenta_clave_cambiada
                        : R.string.cuenta_clave_incorrecta));
            });
        });

        getSupportFragmentManager().setFragmentResultListener(BORRAR_CUENTA, this, (k, r) ->
                AppDatabase.executor.execute(() -> {
                    ecoDB.borrarUsuario(usuarioId);  // playlists y canciones en cascada
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.cuenta_eliminada, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }));
    }

    /** Toast desde el executor (los Toast también son del hilo principal). */
    private void aviso(String texto) {
        runOnUiThread(() -> Toast.makeText(this, texto, Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
