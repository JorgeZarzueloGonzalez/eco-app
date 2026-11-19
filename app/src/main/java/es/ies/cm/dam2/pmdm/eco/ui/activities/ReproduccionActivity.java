package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.google.android.material.slider.Slider;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.data.room.AppDatabase;
import es.ies.cm.dam2.pmdm.eco.data.room.CancionDao;
import es.ies.cm.dam2.pmdm.eco.data.room.ReproduccionDao;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import es.ies.cm.dam2.pmdm.eco.model.Reproduccion;
import es.ies.cm.dam2.pmdm.eco.ui.adapters.AdaptadorReproduccion;
import java.util.ArrayList;
import java.util.List;

/**
 * P13 Reproducciones: la {@code PartidaActivity} del enunciado.
 * <ul>
 *     <li>El {@code Spinner} carga las canciones de Room.</li>
 *     <li>Al elegir una, se muestran sus reproducciones y el total
 *     ({@link ReproduccionDao#porCancion} y {@link ReproduccionDao#contar}).</li>
 *     <li>Guardar crea una {@link Reproduccion} con la fecha actual, la
 *     puntuación del slider (1-9) y el oyente.</li>
 *     <li>Clic largo en una reproducción: borrarla.</li>
 * </ul>
 * Todas las consultas van en {@link AppDatabase#executor}.
 */
public class ReproduccionActivity extends AppCompatActivity {

    private final ArrayList<Cancion> canciones = new ArrayList<>();
    private final ArrayList<Reproduccion> reproducciones = new ArrayList<>();

    private CancionDao cancionDao;
    private ReproduccionDao reproduccionDao;
    private AdaptadorReproduccion adaptador;

    private Spinner cancionesSpinner;
    private TextView totalTexto;
    private TextView vacioTexto;
    private EditText oyenteEdit;
    private Slider puntuacionSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reproduccion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.reproducciones);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        AppDatabase bd = AppDatabase.getInstancia(this);
        cancionDao = bd.cancionDao();
        reproduccionDao = bd.reproduccionDao();

        cancionesSpinner = findViewById(R.id.cancionesReproduccionSpinner);
        totalTexto = findViewById(R.id.totalReproducciones);
        vacioTexto = findViewById(R.id.reproduccionesVacio);
        oyenteEdit = findViewById(R.id.oyenteReproduccionEdit);
        puntuacionSlider = findViewById(R.id.puntuacionReproduccionSlider);
        TextView puntuacionTexto = findViewById(R.id.puntuacionReproduccionTexto);

        puntuacionTexto.setText(getString(R.string.reproduccion_puntuacion,
                (int) puntuacionSlider.getValue()));
        puntuacionSlider.addOnChangeListener((slider, valor, delUsuario) ->
                puntuacionTexto.setText(getString(R.string.reproduccion_puntuacion, (int) valor)));

        RecyclerView recycler = findViewById(R.id.reproduccionesRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorReproduccion(reproducciones, this::confirmarBorrado);
        recycler.setAdapter(adaptador);

        // Un Spinner se escucha con OnItemSelectedListener, no con setOnClickListener.
        cancionesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int posicion, long id) {
                cargarReproducciones();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se usa
            }
        });

        findViewById(R.id.guardarReproduccionButton).setOnClickListener(v -> guardar());
    }

    /** Al volver (por ejemplo, tras borrar una canción en la biblioteca) se recarga todo. */
    @Override
    protected void onResume() {
        super.onResume();
        cargarCanciones();
    }

    private void cargarCanciones() {
        AppDatabase.executor.execute(() -> {
            List<Cancion> leidas = cancionDao.todas();
            runOnUiThread(() -> {
                // Se intenta mantener la misma canción elegida tras recargar.
                Cancion antes = cancionElegida();
                canciones.clear();
                canciones.addAll(leidas);

                List<String> nombres = new ArrayList<>();
                int posicion = 0;
                for (int i = 0; i < canciones.size(); i++) {
                    nombres.add(canciones.get(i).getNombre());
                    if (antes != null && canciones.get(i).getId() == antes.getId()) {
                        posicion = i;
                    }
                }

                ArrayAdapter<String> adaptadorSpinner = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, nombres);
                adaptadorSpinner.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                cancionesSpinner.setAdapter(adaptadorSpinner);

                if (canciones.isEmpty()) {
                    totalTexto.setText(R.string.reproduccion_sin_canciones);
                    reproducciones.clear();
                    adaptador.notifyDataSetChanged();
                    vacioTexto.setVisibility(View.GONE);
                } else {
                    cancionesSpinner.setSelection(posicion);
                    cargarReproducciones();
                }
            });
        });
    }

    /** Lista y total de la canción elegida en el Spinner. */
    private void cargarReproducciones() {
        Cancion cancion = cancionElegida();
        if (cancion == null) {
            return;
        }

        AppDatabase.executor.execute(() -> {
            List<Reproduccion> leidas = reproduccionDao.porCancion(cancion.getId());
            int total = reproduccionDao.contar(cancion.getId());
            runOnUiThread(() -> {
                reproducciones.clear();
                reproducciones.addAll(leidas);
                adaptador.notifyDataSetChanged();
                totalTexto.setText(getString(R.string.reproduccion_total, total));
                vacioTexto.setVisibility(leidas.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void guardar() {
        Cancion cancion = cancionElegida();
        if (cancion == null) {
            Toast.makeText(this, R.string.reproduccion_sin_canciones, Toast.LENGTH_SHORT).show();
            return;
        }

        String oyente = oyenteEdit.getText() == null ? "" : oyenteEdit.getText().toString().trim();
        if (oyente.isEmpty()) {
            oyenteEdit.setError(getString(R.string.reproduccion_falta_oyente));
            return;
        }

        Reproduccion nueva = new Reproduccion(cancion.getId(), System.currentTimeMillis(),
                (int) puntuacionSlider.getValue(), oyente);

        AppDatabase.executor.execute(() -> {
            reproduccionDao.insertar(nueva);
            runOnUiThread(() -> {
                oyenteEdit.setText("");
                Toast.makeText(this, R.string.reproduccion_guardada, Toast.LENGTH_SHORT).show();
            });
        });
        cargarReproducciones(); // se encola detrás del insert
    }

    private void confirmarBorrado(Reproduccion reproduccion) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.reproduccion_borrar_pregunta)
                .setPositiveButton(R.string.borrar, (dialog, which) -> {
                    AppDatabase.executor.execute(() -> reproduccionDao.borrar(reproduccion));
                    cargarReproducciones();
                    Toast.makeText(this, R.string.reproduccion_borrada, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private Cancion cancionElegida() {
        int posicion = cancionesSpinner.getSelectedItemPosition();
        return posicion >= 0 && posicion < canciones.size() ? canciones.get(posicion) : null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
