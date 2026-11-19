package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.data.room.AppDatabase;
import es.ies.cm.dam2.pmdm.eco.data.room.CancionDao;
import es.ies.cm.dam2.pmdm.eco.data.sqlite.EcoDB;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import es.ies.cm.dam2.pmdm.eco.ui.adapters.AdaptadorCancionRecycler;
import es.ies.cm.dam2.pmdm.eco.ui.dialogs.ConfirmarDialogFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * Proyecto: las canciones de una playlist.
 * <p>
 * Combina las dos bases de datos: la playlist y sus {@code cancion_id} están
 * en SQLite ({@code eco.db}) y los datos de cada canción en Room
 * ({@code eco_room.db}). Como no hay JOIN posible entre dos bases de datos,
 * cada id se resuelve con {@link CancionDao#porId}; si la canción se ha
 * borrado, se quita de la playlist (huérfana).
 * <p>
 * <b>Spinner dependientes</b>: el primero carga los géneros que hay en Room;
 * al elegir uno, el segundo carga solo las canciones de ese género.
 */
public class PlaylistActivity extends AppCompatActivity
        implements AdaptadorCancionRecycler.OyenteCancion {

    /** Extra con el {@code _id} de la playlist. */
    public static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";

    private static final String QUITAR = "playlist_quitar";
    private static final String ESTADO_GENERO = "genero";
    private static final String ESTADO_CANCION = "cancion";

    private final ArrayList<Cancion> enPlaylist = new ArrayList<>();
    private final ArrayList<String> generos = new ArrayList<>();
    private final ArrayList<Cancion> delGenero = new ArrayList<>();

    private EcoDB ecoDB;
    private CancionDao cancionDao;
    private AdaptadorCancionRecycler adaptador;
    private MaterialToolbar toolbar;
    private Spinner generoSpinner, cancionSpinner;
    private TextView vacia;
    private int playlistId;

    /** Lo que había elegido antes de girar: se vuelve a seleccionar al cargar. */
    private String generoPendiente;
    private int cancionPendiente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playlist);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        playlistId = getIntent().getIntExtra(EXTRA_PLAYLIST_ID, 0);
        ecoDB = EcoDB.getInstance(this);
        cancionDao = AppDatabase.getInstancia(this).cancionDao();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        generoSpinner = findViewById(R.id.generoPlaylistSpinner);
        cancionSpinner = findViewById(R.id.cancionPlaylistSpinner);
        vacia = findViewById(R.id.playlistVacia);

        if (savedInstanceState != null) {
            generoPendiente = savedInstanceState.getString(ESTADO_GENERO);
            cancionPendiente = savedInstanceState.getInt(ESTADO_CANCION);
        }

        RecyclerView recycler = findViewById(R.id.playlistRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorCancionRecycler(enPlaylist, this);
        recycler.setAdapter(adaptador);

        // Spinner 1 -> Spinner 2: al cambiar el género se recargan las canciones
        generoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int posicion, long id) {
                cargarCancionesDe(generos.get(posicion));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se usa
            }
        });

        findViewById(R.id.anadirPlaylistButton).setOnClickListener(v -> anadir());

        getSupportFragmentManager().setFragmentResultListener(QUITAR, this,
                (k, r) -> quitar(r.getInt(ConfirmarDialogFragment.RESULTADO_ID)));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int g = generoSpinner.getSelectedItemPosition();
        int c = cancionSpinner.getSelectedItemPosition();
        outState.putString(ESTADO_GENERO, g >= 0 && g < generos.size() ? generos.get(g) : null);
        outState.putInt(ESTADO_CANCION, c >= 0 && c < delGenero.size() ? delGenero.get(c).getId() : 0);
    }

    /** Al volver (de la ficha, o tras borrar canciones) se recarga todo. */
    @Override
    protected void onResume() {
        super.onResume();
        cargarPlaylist();
        cargarGeneros();
    }

    // ===================== carga =====================

    /** Nombre y canciones de la playlist: SQLite para los ids, Room para los datos. */
    private void cargarPlaylist() {
        AppDatabase.executor.execute(() -> {
            String nombre = ecoDB.getNombrePlaylist(playlistId);
            List<Cancion> canciones = new ArrayList<>();
            if (nombre != null) {
                for (int id : ecoDB.getCancionIds(playlistId)) {
                    Cancion c = cancionDao.porId(id);
                    if (c != null) {
                        canciones.add(c);
                    } else {
                        ecoDB.quitarCancion(playlistId, id); // huérfana: se borró en Room
                    }
                }
            }
            runOnUiThread(() -> {
                if (nombre == null) {   // la playlist ya no existe
                    finish();
                    return;
                }
                toolbar.setTitle(nombre);
                enPlaylist.clear();
                enPlaylist.addAll(canciones);
                adaptador.notifyDataSetChanged();
                vacia.setVisibility(enPlaylist.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    /** Spinner 1: los géneros distintos que hay en Room. */
    private void cargarGeneros() {
        AppDatabase.executor.execute(() -> {
            List<String> leidos = cancionDao.generos();
            runOnUiThread(() -> {
                String antes = generoPendiente != null ? generoPendiente : generoElegido();
                generos.clear();
                generos.addAll(leidos);
                generoSpinner.setAdapter(adaptadorDe(generos));
                int posicion = generos.indexOf(antes);
                if (posicion >= 0) {
                    generoSpinner.setSelection(posicion);
                }
                generoPendiente = null;
                // setAdapter dispara onItemSelected, que carga el Spinner 2
            });
        });
    }

    /** Spinner 2: las canciones del género elegido en el Spinner 1. */
    private void cargarCancionesDe(String genero) {
        AppDatabase.executor.execute(() -> {
            List<Cancion> leidas = cancionDao.porGenero(genero);
            runOnUiThread(() -> {
                delGenero.clear();
                delGenero.addAll(leidas);
                List<String> nombres = new ArrayList<>();
                int posicion = 0;
                for (int i = 0; i < delGenero.size(); i++) {
                    nombres.add(delGenero.get(i).getNombre());
                    if (delGenero.get(i).getId() == cancionPendiente) {
                        posicion = i;
                    }
                }
                cancionSpinner.setAdapter(adaptadorDe(nombres));
                cancionSpinner.setSelection(posicion);
                cancionPendiente = 0;
            });
        });
    }

    // ===================== añadir y quitar =====================

    private void anadir() {
        int posicion = cancionSpinner.getSelectedItemPosition();
        if (posicion < 0 || posicion >= delGenero.size()) {
            return;
        }
        Cancion cancion = delGenero.get(posicion);
        AppDatabase.executor.execute(() -> {
            long id = ecoDB.anadirCancion(playlistId, cancion.getId());
            runOnUiThread(() -> Toast.makeText(this, id == -1
                    ? getString(R.string.playlist_ya_esta)
                    : getString(R.string.playlist_anadida, cancion.getNombre()),
                    Toast.LENGTH_SHORT).show());
        });
        cargarPlaylist();
    }

    /** Clic corto: la ficha de la canción. */
    @Override
    public void alHacerClicCorto(int posicion) {
        if (posicion == RecyclerView.NO_POSITION) return;
        Intent intent = new Intent(this, FichaCancionActivity.class);
        intent.putExtra(BibliotecaActivity.EXTRA_CANCION, enPlaylist.get(posicion));
        startActivity(intent);
    }

    /** Clic largo: quitarla de la playlist (con confirmación). */
    @Override
    public void alHacerClicLargo(int posicion, View itemview) {
        if (posicion == RecyclerView.NO_POSITION) return;
        Cancion c = enPlaylist.get(posicion);
        ConfirmarDialogFragment.mostrar(getSupportFragmentManager(), QUITAR,
                getString(R.string.playlist_quitar),
                getString(R.string.playlist_quitar_pregunta, c.getNombre()), c.getId());
    }

    private void quitar(int cancionId) {
        AppDatabase.executor.execute(() -> {
            ecoDB.quitarCancion(playlistId, cancionId);
            runOnUiThread(() ->
                    Toast.makeText(this, R.string.playlist_quitada, Toast.LENGTH_SHORT).show());
        });
        cargarPlaylist();
    }

    // ===================== ayudas =====================

    private String generoElegido() {
        int p = generoSpinner.getSelectedItemPosition();
        return p >= 0 && p < generos.size() ? generos.get(p) : null;
    }

    private ArrayAdapter<String> adaptadorDe(List<String> datos) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>(datos));
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
