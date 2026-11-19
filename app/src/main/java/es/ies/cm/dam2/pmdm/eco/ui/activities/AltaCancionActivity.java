package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.data.room.AppDatabase;
import es.ies.cm.dam2.pmdm.eco.data.room.CancionDao;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import java.util.Calendar;

/**
 * Alta y edición de una canción.
 * <ul>
 *     <li><b>Alta</b>: se abre sin extras; Guardar inserta en Room.</li>
 *     <li><b>Edición</b>: se abre con la canción en {@link #EXTRA_CANCION}
 *     (desde la ficha o el menú contextual de la biblioteca); Guardar actualiza
 *     la misma fila (mismo {@code id}).</li>
 * </ul>
 * Componentes: EditText con validación, Spinner de género, contador de
 * intérpretes, RatingBar (valoración), RadioGroup (estado), Switch (favorita)
 * y CheckBox (explícita). Todo se guarda y se conserva al reabrir la app.
 */
public class AltaCancionActivity extends AppCompatActivity {

    /** Extra con la {@link Cancion} a editar. */
    public static final String EXTRA_CANCION = "extra_cancion";

    private static final int MIN_INTERPRETES = 1;
    private static final int MAX_INTERPRETES = 4;
    private static final int ANIO_MINIMO = 1900;

    /** Clave para guardar el contador al girar (es la única parte que Android no guarda solo). */
    private static final String ESTADO_INTERPRETES = "interpretes";

    private CancionDao cancionDao;

    /** {@code null} en un alta; la canción original en una edición. */
    private Cancion original;
    private int interpretes = MIN_INTERPRETES;

    private TextInputLayout nombreLayout, selloLayout, anioLayout, duracionLayout, popularidadLayout;
    private TextInputEditText nombreEdit, selloEdit, anioEdit, duracionEdit, popularidadEdit;
    private Spinner generoSpinner;
    private ArrayAdapter<CharSequence> generos;
    private TextView interpretesTexto, valoracionTexto;
    private Button menosButton, masButton;
    private RatingBar valoracionRating;
    private RadioGroup estadoGroup;
    private MaterialSwitch favoritaSwitch;
    private CheckBox explicitaCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alta_cancion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        cancionDao = AppDatabase.getInstancia(this).cancionDao();
        original = (Cancion) getIntent().getSerializableExtra(EXTRA_CANCION);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(original == null ? R.string.alta_titulo : R.string.editar_cancion);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // flecha atrás = cancelar
        }

        enlazarVistas();
        prepararSpinner();
        prepararContador();
        prepararValidaciones();

        valoracionRating.setOnRatingBarChangeListener((barra, valor, delUsuario) -> pintarValoracion());
        findViewById(R.id.limpiarButton).setOnClickListener(v -> limpiar());
        findViewById(R.id.guardarButton).setOnClickListener(v -> guardar());

        if (savedInstanceState != null) {
            // Viene de un giro: los EditText, el RatingBar, el RadioGroup, el Switch
            // y el CheckBox se restauran solos (tienen id); el contador no.
            interpretes = savedInstanceState.getInt(ESTADO_INTERPRETES, MIN_INTERPRETES);
        } else if (original != null) {
            cargar(original);
        }
        pintarContador();
        pintarValoracion();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_INTERPRETES, interpretes);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ===================== preparación =====================

    private void enlazarVistas() {
        nombreLayout = findViewById(R.id.nombreLayout);
        selloLayout = findViewById(R.id.selloLayout);
        anioLayout = findViewById(R.id.anioLayout);
        duracionLayout = findViewById(R.id.duracionLayout);
        popularidadLayout = findViewById(R.id.popularidadLayout);
        nombreEdit = findViewById(R.id.nombreEdit);
        selloEdit = findViewById(R.id.selloEdit);
        anioEdit = findViewById(R.id.anioEdit);
        duracionEdit = findViewById(R.id.duracionEdit);
        popularidadEdit = findViewById(R.id.popularidadEdit);
        generoSpinner = findViewById(R.id.generoSpinner);
        interpretesTexto = findViewById(R.id.interpretesTexto);
        valoracionTexto = findViewById(R.id.valoracionTexto);
        menosButton = findViewById(R.id.menosButton);
        masButton = findViewById(R.id.masButton);
        valoracionRating = findViewById(R.id.valoracionRating);
        estadoGroup = findViewById(R.id.estadoGroup);
        favoritaSwitch = findViewById(R.id.favoritaSwitch);
        explicitaCheck = findViewById(R.id.explicitaCheck);
    }

    /** El Spinner se rellena desde res/values/arrays.xml; la posición 0 es "Selecciona un género". */
    private void prepararSpinner() {
        generos = ArrayAdapter.createFromResource(this, R.array.generos,
                android.R.layout.simple_spinner_item);
        generos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        generoSpinner.setAdapter(generos);
    }

    /** Contador de intérpretes (P2): los botones se apagan al llegar a los límites. */
    private void prepararContador() {
        menosButton.setOnClickListener(v -> {
            if (interpretes > MIN_INTERPRETES) {
                interpretes--;
                pintarContador();
            }
        });
        masButton.setOnClickListener(v -> {
            if (interpretes < MAX_INTERPRETES) {
                interpretes++;
                pintarContador();
            }
        });
    }

    private void pintarContador() {
        interpretesTexto.setText(String.valueOf(interpretes));
        menosButton.setEnabled(interpretes > MIN_INTERPRETES);
        masButton.setEnabled(interpretes < MAX_INTERPRETES);
    }

    private void pintarValoracion() {
        int valor = Math.round(valoracionRating.getRating());
        valoracionTexto.setText(valor == 0
                ? getString(R.string.alta_valoracion)
                : getString(R.string.alta_valoracion_valor, valor));
    }

    /** Al escribir, se borra el error de ese campo (el aviso solo sale al pulsar Guardar). */
    private void prepararValidaciones() {
        quitarErrorAlEscribir(nombreEdit, nombreLayout);
        quitarErrorAlEscribir(selloEdit, selloLayout);
        quitarErrorAlEscribir(anioEdit, anioLayout);
        quitarErrorAlEscribir(duracionEdit, duracionLayout);
        quitarErrorAlEscribir(popularidadEdit, popularidadLayout);
    }

    private void quitarErrorAlEscribir(TextInputEditText campo, TextInputLayout layout) {
        campo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se usa
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se usa
            }
        });
    }

    // ===================== cargar, validar y guardar =====================

    /** Modo edición: rellena todos los controles con la canción. */
    private void cargar(Cancion c) {
        nombreEdit.setText(c.getNombre());
        selloEdit.setText(c.getSello());
        anioEdit.setText(String.valueOf(c.getAnio()));
        duracionEdit.setText(String.valueOf(c.getDuracion()));
        popularidadEdit.setText(String.valueOf(c.getPopularidad()));
        interpretes = Math.max(MIN_INTERPRETES, Math.min(MAX_INTERPRETES, c.getInterpretes()));

        int posicion = generos.getPosition(c.getGenero());
        generoSpinner.setSelection(Math.max(posicion, 0));

        valoracionRating.setRating(c.getValoracion());
        estadoGroup.check(idDeEstado(c.getEstado()));
        favoritaSwitch.setChecked(c.isFavorita());
        explicitaCheck.setChecked(c.isExplicita());
    }

    private void limpiar() {
        nombreEdit.setText("");
        selloEdit.setText("");
        anioEdit.setText("");
        duracionEdit.setText("");
        popularidadEdit.setText("");
        generoSpinner.setSelection(0);
        interpretes = MIN_INTERPRETES;
        pintarContador();
        valoracionRating.setRating(0);
        estadoGroup.check(R.id.estadoNueva);
        favoritaSwitch.setChecked(false);
        explicitaCheck.setChecked(false);
        nombreEdit.requestFocus();
    }

    /** Valida y guarda en Room: insert si es nueva, update si es una edición. */
    private void guardar() {
        Cancion cancion = validar();
        if (cancion == null) {
            Toast.makeText(this, R.string.wrong_data, Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.executor.execute(() -> {
            int mensaje;
            if (cancion.getId() > 0) {
                cancionDao.actualizar(cancion);
                mensaje = R.string.cancion_actualizada;
            } else {
                cancionDao.insertar(cancion);
                mensaje = R.string.cancion_guardada;
            }
            runOnUiThread(() -> {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                finish(); // la pantalla anterior recarga en onResume
            });
        });
    }

    /**
     * Comprueba cada campo y marca en rojo los incorrectos.
     *
     * @return la canción lista para guardar, o {@code null} si hay algún error.
     */
    private Cancion validar() {
        boolean ok = true;
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);

        String nombre = texto(nombreEdit);
        if (nombre.isEmpty()) {
            nombreLayout.setError(getString(R.string.error_obligatorio));
            ok = false;
        }
        String sello = texto(selloEdit);
        if (sello.isEmpty()) {
            selloLayout.setError(getString(R.string.error_obligatorio));
            ok = false;
        }
        int anio = entero(anioEdit);
        if (anio < ANIO_MINIMO || anio > anioActual) {
            anioLayout.setError(getString(R.string.error_anio, ANIO_MINIMO, anioActual));
            ok = false;
        }
        int duracion = entero(duracionEdit);
        if (duracion <= 0) {
            duracionLayout.setError(getString(R.string.error_duracion));
            ok = false;
        }
        int popularidad = entero(popularidadEdit);
        if (popularidad < 1 || popularidad > 10) {
            popularidadLayout.setError(getString(R.string.popularity_error));
            ok = false;
        }
        if (generoSpinner.getSelectedItemPosition() <= 0) {
            Toast.makeText(this, R.string.error_genero, Toast.LENGTH_SHORT).show();
            ok = false;
        }
        int valoracion = Math.round(valoracionRating.getRating());
        if (valoracion == 0) {
            Toast.makeText(this, R.string.error_valoracion, Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (!ok) {
            return null;
        }

        Cancion c = new Cancion(original == null ? 0 : original.getId(), nombre,
                generoSpinner.getSelectedItem().toString(), sello, interpretes, anio,
                popularidad, duracion, valoracion);
        c.setEstado(estadoElegido());
        c.setFavorita(favoritaSwitch.isChecked());
        c.setExplicita(explicitaCheck.isChecked());
        return c;
    }

    // ===================== ayudas =====================

    private int estadoElegido() {
        int id = estadoGroup.getCheckedRadioButtonId();
        if (id == R.id.estadoEscuchada) return Cancion.ESTADO_ESCUCHADA;
        if (id == R.id.estadoEnBucle) return Cancion.ESTADO_EN_BUCLE;
        return Cancion.ESTADO_NUEVA;
    }

    private static int idDeEstado(int estado) {
        if (estado == Cancion.ESTADO_ESCUCHADA) return R.id.estadoEscuchada;
        if (estado == Cancion.ESTADO_EN_BUCLE) return R.id.estadoEnBucle;
        return R.id.estadoNueva;
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    /** El número escrito, o -1 si el campo está vacío o no es un número. */
    private static int entero(TextInputEditText campo) {
        String t = texto(campo);
        if (TextUtils.isEmpty(t)) {
            return -1;
        }
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
