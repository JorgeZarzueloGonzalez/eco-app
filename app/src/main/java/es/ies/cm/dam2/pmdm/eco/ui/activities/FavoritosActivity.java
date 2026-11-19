package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.data.room.AppDatabase;
import es.ies.cm.dam2.pmdm.eco.data.room.CancionDao;
import es.ies.cm.dam2.pmdm.eco.data.sqlite.EcoDB;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import es.ies.cm.dam2.pmdm.eco.model.Favorito;
import es.ies.cm.dam2.pmdm.eco.model.Usuario;
import es.ies.cm.dam2.pmdm.eco.ui.adapters.AdaptadorCancionRecycler;
import java.util.ArrayList;
import java.util.List;

/**
 * P12 Favoritos: la lista de canciones favoritas de un usuario.
 * <p>
 * El {@code Spinner} carga todas las canciones y el botón añade la
 * seleccionada a la tabla {@code favoritos}.
 * <p>
 * P13: los favoritos siguen en SQLite ({@code eco.db}) pero las canciones
 * están en Room ({@code eco_room.db}). Como no hay JOIN entre dos bases de
 * datos, se leen los favoritos y cada {@code cancion_id} se resuelve con
 * {@link CancionDao#porId}. Si devuelve {@code null} (la canción se borró),
 * ese favorito huérfano se elimina. La lista se pinta con el
 * adaptador de la biblioteca (nombre, género e intérpretes); el clic corto abre
 * la ficha y el clic largo quita la canción de favoritos.
 */
public class FavoritosActivity extends AppCompatActivity
        implements AdaptadorCancionRecycler.OyenteCancion {

    /** Clave del extra con el {@code _id} del usuario que ha hecho login. */
    public static final String EXTRA_USUARIO_ID = "extra_usuario_id";

    /** Todas las canciones (para el Spinner) y solo las favoritas (para la lista). */
    private final ArrayList<Cancion> todas = new ArrayList<>();
    private final ArrayList<Cancion> favoritas = new ArrayList<>();

    private CancionDao cancionDao;
    private EcoDB ecoDB;
    private AdaptadorCancionRecycler adaptador;
    private ArrayAdapter<String> adaptadorSpinner;
    private Spinner cancionesSpinner;
    private TextView favoritosVacio;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favoritos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usuarioId = getIntent().getIntExtra(EXTRA_USUARIO_ID, -1);
        if (usuarioId <= 0) {
            // Sin usuario no hay lista que enseñar: se vuelve al login.
            finish();
            return;
        }

        cancionDao = AppDatabase.getInstancia(this).cancionDao();
        ecoDB = EcoDB.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.favoritos_titulo_corto);
        setSupportActionBar(toolbar);

        AppDatabase.executor.execute(() -> {
            Usuario usuario = ecoDB.getUsuario(usuarioId);
            if (usuario != null) {
                runOnUiThread(() -> toolbar.setTitle(
                        getString(R.string.favoritos_titulo, usuario.getNombre())));
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        cancionesSpinner = findViewById(R.id.cancionesFavoritosSpinner);
        favoritosVacio = findViewById(R.id.favoritosVacio);

        RecyclerView favoritosRecycler = findViewById(R.id.favoritosRecycler);
        favoritosRecycler.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorCancionRecycler(favoritas, this);
        favoritosRecycler.setAdapter(adaptador);

        findViewById(R.id.anadirFavoritoButton).setOnClickListener(v -> anadirFavorito());
    }

    /** Al volver de la ficha se recargan las canciones y los favoritos. */
    @Override
    protected void onResume() {
        super.onResume();
        if (adaptador != null) {
            cargarTodo();
        }
    }

    /**
     * Rellena el Spinner con todas las canciones (Room) y la lista con las
     * favoritas (SQLite + Room). Todo se lee en el executor y se pinta después
     * en el hilo principal.
     */
    private void cargarTodo() {
        AppDatabase.executor.execute(() -> {
            List<Cancion> canciones = cancionDao.todas();

            List<Cancion> deUsuario = new ArrayList<>();
            for (Favorito favorito : ecoDB.getFavoritos(usuarioId)) {
                Cancion cancion = cancionDao.porId(favorito.getCancionId());
                if (cancion != null) {
                    deUsuario.add(cancion);
                } else {
                    // Huérfano: la canción se borró en Room y aquí no hay CASCADE.
                    ecoDB.borrarFavorito(favorito.getId());
                }
            }

            runOnUiThread(() -> pintar(canciones, deUsuario));
        });
    }

    private void pintar(List<Cancion> canciones, List<Cancion> deUsuario) {
        todas.clear();
        todas.addAll(canciones);

        List<String> nombres = new ArrayList<>();
        for (Cancion cancion : todas) {
            nombres.add(cancion.getNombre());
        }

        int seleccion = cancionesSpinner.getSelectedItemPosition();
        adaptadorSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombres);
        adaptadorSpinner.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        cancionesSpinner.setAdapter(adaptadorSpinner);
        if (seleccion >= 0 && seleccion < nombres.size()) {
            cancionesSpinner.setSelection(seleccion);
        }

        favoritas.clear();
        favoritas.addAll(deUsuario);
        adaptador.notifyDataSetChanged();

        favoritosVacio.setVisibility(favoritas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Guarda la canción elegida en el Spinner como favorita del usuario. */
    private void anadirFavorito() {
        int posicion = cancionesSpinner.getSelectedItemPosition();
        if (posicion < 0 || posicion >= todas.size()) {
            return;
        }

        Cancion cancion = todas.get(posicion);
        AppDatabase.executor.execute(() -> {
            long id = ecoDB.insertarFavorito(usuarioId, cancion.getId());
            runOnUiThread(() -> {
                if (id == -1) {
                    Toast.makeText(this, R.string.favoritos_duplicada, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.favoritos_anadida, cancion.getNombre()),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
        cargarTodo();
    }

    @Override
    public void alHacerClicCorto(int posicion) {
        if (posicion == RecyclerView.NO_POSITION) return;

        Intent intent = new Intent(this, FichaCancionActivity.class);
        intent.putExtra(BibliotecaActivity.EXTRA_CANCION, favoritas.get(posicion));
        startActivity(intent);
    }

    @Override
    public void alHacerClicLargo(int posicion, View itemview) {
        if (posicion == RecyclerView.NO_POSITION) return;

        Cancion cancion = favoritas.get(posicion);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.favoritos_quitar)
                .setMessage(getString(R.string.favoritos_quitar_pregunta, cancion.getNombre()))
                .setPositiveButton(R.string.quitar, (dialog, which) -> quitarFavorito(cancion))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void quitarFavorito(Cancion cancion) {
        AppDatabase.executor.execute(() ->
                ecoDB.borrarFavorito(usuarioId, cancion.getId()));
        cargarTodo();
        Toast.makeText(this, R.string.favoritos_quitada, Toast.LENGTH_SHORT).show();
    }

    /** La flecha de la toolbar vuelve a la pantalla anterior. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
