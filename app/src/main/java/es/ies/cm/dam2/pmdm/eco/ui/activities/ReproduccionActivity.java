package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.material.slider.Slider;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.data.room.AppDatabase;
import es.ies.cm.dam2.pmdm.eco.data.room.CancionDao;
import es.ies.cm.dam2.pmdm.eco.data.room.ReproduccionDao;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import es.ies.cm.dam2.pmdm.eco.model.Reproduccion;
import es.ies.cm.dam2.pmdm.eco.ui.adapters.AdaptadorReproduccion;
import es.ies.cm.dam2.pmdm.eco.ui.dialogs.ConfirmarDialogFragment;
import es.ies.cm.dam2.pmdm.eco.ui.dialogs.FechaDialogFragment;
import es.ies.cm.dam2.pmdm.eco.ui.dialogs.HoraDialogFragment;
import es.ies.cm.dam2.pmdm.eco.util.Formatos;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * P13 + proyecto: las reproducciones de cada canción (la entidad relacionada
 * 1-N de Room, con CASCADE).
 * <ul>
 *     <li>Spinner con las canciones de Room; al elegir una se ven sus
 *     reproducciones y el total.</li>
 *     <li>Formulario: oyente, <b>fecha y hora elegidas con DatePicker y
 *     TimePicker</b> (dos DialogFragment) y puntuación con un Slider.</li>
 *     <li>CRUD completo: Guardar (alta), clic corto (editar), clic largo (borrar
 *     con confirmación).</li>
 * </ul>
 */
public class ReproduccionActivity extends AppCompatActivity
        implements AdaptadorReproduccion.OyenteReproduccion {

    /** Extra opcional: el id de la canción que se quiere ver (desde la ficha). */
    public static final String EXTRA_CANCION_ID = "extra_cancion_id";

    private static final String BORRAR = "reproduccion_borrar";
    private static final String ESTADO_FECHA = "fecha";
    private static final String ESTADO_EDITANDO = "editando";
    private static final String ESTADO_CANCION = "cancion";

    private final ArrayList<Cancion> canciones = new ArrayList<>();
    private final ArrayList<Reproduccion> reproducciones = new ArrayList<>();

    private CancionDao cancionDao;
    private ReproduccionDao reproduccionDao;
    private AdaptadorReproduccion adaptador;

    private Spinner cancionesSpinner;
    private TextView totalTexto, vacioTexto, fechaTexto, puntuacionTexto;
    private EditText oyenteEdit;
    private Slider puntuacionSlider;
    private Button guardarButton, cancelarButton;

    /** Fecha y hora de la reproducción que se está escribiendo (milisegundos). */
    private long fechaElegida = System.currentTimeMillis();
    /** Id de la reproducción que se está editando; 0 si es nueva. */
    private int editandoId = 0;
    /** Canción que hay que dejar seleccionada al cargar el Spinner. */
    private int cancionInicialId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reproduccion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
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
        fechaTexto = findViewById(R.id.fechaReproduccionTexto);
        puntuacionTexto = findViewById(R.id.puntuacionReproduccionTexto);
        oyenteEdit = findViewById(R.id.oyenteReproduccionEdit);
        puntuacionSlider = findViewById(R.id.puntuacionReproduccionSlider);
        guardarButton = findViewById(R.id.guardarReproduccionButton);
        cancelarButton = findViewById(R.id.cancelarEdicionButton);

        // Estado que hay que recuperar si se ha girado el móvil
        cancionInicialId = getIntent().getIntExtra(EXTRA_CANCION_ID, 0);
        if (savedInstanceState != null) {
            fechaElegida = savedInstanceState.getLong(ESTADO_FECHA, fechaElegida);
            editandoId = savedInstanceState.getInt(ESTADO_EDITANDO, 0);
            cancionInicialId = savedInstanceState.getInt(ESTADO_CANCION, cancionInicialId);
        }

        puntuacionSlider.addOnChangeListener((slider, valor, delUsuario) -> pintarPuntuacion());
        pintarPuntuacion();
        pintarFecha();
        pintarModo();

        RecyclerView recycler = findViewById(R.id.reproduccionesRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorReproduccion(reproducciones, this);
        recycler.setAdapter(adaptador);

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

        // Fecha: primero el DatePicker; al elegir el día se abre el TimePicker
        findViewById(R.id.fechaReproduccionButton).setOnClickListener(v ->
                FechaDialogFragment.mostrar(getSupportFragmentManager(), fechaElegida));
        getSupportFragmentManager().setFragmentResultListener(FechaDialogFragment.CLAVE, this,
                (clave, r) -> {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(fechaElegida);
                    c.set(r.getInt(FechaDialogFragment.ANIO), r.getInt(FechaDialogFragment.MES),
                            r.getInt(FechaDialogFragment.DIA));
                    fechaElegida = c.getTimeInMillis();
                    pintarFecha();
                    HoraDialogFragment.mostrar(getSupportFragmentManager(), fechaElegida);
                });
        getSupportFragmentManager().setFragmentResultListener(HoraDialogFragment.CLAVE, this,
                (clave, r) -> {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(fechaElegida);
                    c.set(Calendar.HOUR_OF_DAY, r.getInt(HoraDialogFragment.HORA));
                    c.set(Calendar.MINUTE, r.getInt(HoraDialogFragment.MINUTO));
                    fechaElegida = c.getTimeInMillis();
                    pintarFecha();
                });

        getSupportFragmentManager().setFragmentResultListener(BORRAR, this,
                (clave, r) -> borrar(r.getInt(ConfirmarDialogFragment.RESULTADO_ID)));

        guardarButton.setOnClickListener(v -> guardar());
        cancelarButton.setOnClickListener(v -> terminarEdicion());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ESTADO_FECHA, fechaElegida);
        outState.putInt(ESTADO_EDITANDO, editandoId);
        Cancion elegida = cancionElegida();
        outState.putInt(ESTADO_CANCION, elegida != null ? elegida.getId() : cancionInicialId);
    }

    /** Al volver (por ejemplo, tras borrar una canción en la biblioteca) se recarga todo. */
    @Override
    protected void onResume() {
        super.onResume();
        cargarCanciones();
    }

    // ===================== carga =====================

    private void cargarCanciones() {
        AppDatabase.executor.execute(() -> {
            List<Cancion> leidas = cancionDao.todas();
            runOnUiThread(() -> {
                // Se intenta mantener la canción que estaba elegida
                Cancion antes = cancionElegida();
                int buscar = antes != null ? antes.getId() : cancionInicialId;

                canciones.clear();
                canciones.addAll(leidas);
                List<String> nombres = new ArrayList<>();
                int posicion = 0;
                for (int i = 0; i < canciones.size(); i++) {
                    nombres.add(canciones.get(i).getNombre());
                    if (canciones.get(i).getId() == buscar) {
                        posicion = i;
                    }
                }

                ArrayAdapter<String> adaptadorSpinner = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, nombres);
                adaptadorSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

    // ===================== alta y edición =====================

    private void guardar() {
        Cancion cancion = cancionElegida();
        if (cancion == null) {
            Toast.makeText(this, R.string.reproduccion_sin_canciones, Toast.LENGTH_SHORT).show();
            return;
        }
        String oyente = oyenteEdit.getText().toString().trim();
        if (oyente.isEmpty()) {
            oyenteEdit.setError(getString(R.string.reproduccion_falta_oyente));
            return;
        }

        Reproduccion r = new Reproduccion(cancion.getId(), fechaElegida,
                (int) puntuacionSlider.getValue(), oyente);
        r.setId(editandoId);
        boolean esEdicion = editandoId > 0;

        AppDatabase.executor.execute(() -> {
            if (esEdicion) {
                reproduccionDao.actualizar(r);
            } else {
                reproduccionDao.insertar(r);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, esEdicion ? R.string.reproduccion_actualizada
                        : R.string.reproduccion_guardada, Toast.LENGTH_SHORT).show();
                terminarEdicion();
            });
        });
        cargarReproducciones(); // se encola detrás del guardado
    }

    /** Clic corto en una reproducción: se carga en el formulario para editarla. */
    @Override
    public void alPulsar(Reproduccion r) {
        editandoId = r.getId();
        fechaElegida = r.getFecha();
        oyenteEdit.setText(r.getOyente());
        puntuacionSlider.setValue(Math.max(1, Math.min(9, r.getPuntuacion())));
        pintarFecha();
        pintarModo();
        oyenteEdit.requestFocus();
    }

    /** Clic largo: confirmar el borrado con el DialogFragment propio. */
    @Override
    public void alPedirBorrado(Reproduccion r) {
        ConfirmarDialogFragment.mostrar(getSupportFragmentManager(), BORRAR,
                getString(R.string.borrar), getString(R.string.reproduccion_borrar_pregunta),
                r.getId());
    }

    private void borrar(int id) {
        AppDatabase.executor.execute(() -> {
            Reproduccion r = reproduccionDao.porId(id);
            if (r != null) {
                reproduccionDao.borrar(r);
            }
            runOnUiThread(() -> {
                if (id == editandoId) {
                    terminarEdicion();
                }
                Toast.makeText(this, R.string.reproduccion_borrada, Toast.LENGTH_SHORT).show();
            });
        });
        cargarReproducciones();
    }

    /** Vuelve al modo "nueva reproducción" con el formulario limpio. */
    private void terminarEdicion() {
        editandoId = 0;
        oyenteEdit.setText("");
        fechaElegida = System.currentTimeMillis();
        puntuacionSlider.setValue(5);
        pintarFecha();
        pintarModo();
    }

    // ===================== pintar =====================

    private void pintarFecha() {
        fechaTexto.setText(Formatos.fechaHora(this, fechaElegida));
    }

    private void pintarPuntuacion() {
        puntuacionTexto.setText(getString(R.string.reproduccion_puntuacion,
                (int) puntuacionSlider.getValue()));
    }

    /** El botón dice "Guardar cambios" y aparece "Cancelar" mientras se edita. */
    private void pintarModo() {
        boolean editando = editandoId > 0;
        guardarButton.setText(editando ? R.string.guardar_cambios : R.string.reproduccion_guardar);
        cancelarButton.setVisibility(editando ? View.VISIBLE : View.GONE);
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
