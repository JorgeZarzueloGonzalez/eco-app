package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

/**
 * Pantalla de alta de una canción: aquí están TODOS los controles de las
 * prácticas 2, 3 y 4 (el enunciado pide unirlos en una sola pantalla).
 * <p>
 * P2: contadores de intérpretes, nombre y "¿en mi biblioteca?".
 * P3: teclado de puntuación, validación de la popularidad y estado con color.
 * P4: formulario completo, validaciones, RESET y ENVIAR a la ficha.
 */
public class AltaCancionActivity extends AppCompatActivity {

    /** Claves de los extras que viajan a {@link FichaCancionActivity}. */
    public static final String EXTRA_NOMBRE = "extra_nombre";
    public static final String EXTRA_GENERO = "extra_genero";
    public static final String EXTRA_SELLO = "extra_sello";
    public static final String EXTRA_INTERPRETES_MIN = "extra_interpretes_min";
    public static final String EXTRA_INTERPRETES_MAX = "extra_interpretes_max";
    public static final String EXTRA_ANIO = "extra_anio";
    public static final String EXTRA_DURACION = "extra_duracion";
    public static final String EXTRA_POPULARIDAD = "extra_popularidad";

    /** Máximo de intérpretes que permite el contador de la P2. */
    private static final int MAX_ARTISTAS = 4;

    /** Ids de las 9 teclas, en orden del 1 al 9. */
    private static final int[] TECLAS = {
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9
    };

    /** Número de intérpretes del contador (atributo de clase: lo usan varias lambdas). */
    private int artistNumberValue = 1;

    /** Último nombre aceptado con el botón de la P2.2 (por si el EditText se vació). */
    private String nombreAceptado = "";

    private View raiz;
    private TextView songName;
    private EditText songNameEdit;
    private Spinner generoSpinner;
    private EditText selloEdit;
    private EditText minEdit;
    private EditText maxEdit;
    private EditText yearEdit;
    private EditText durationEdit;
    private EditText popularityEdit;
    private TextView popularityError;
    private TextView scoreResult;
    private RadioGroup radioGroupState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alta_cancion);

        raiz = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ===================== P2.1 Contador de intérpretes =====================
        TextView artistNumber = findViewById(R.id.artistNumber);
        Button addButton = findViewById(R.id.addButton);
        Button resButton = findViewById(R.id.resButton);

        artistNumber.setText(getString(R.string.numbers_of_artist, artistNumberValue));

        addButton.setOnClickListener(v -> {
            if (artistNumberValue == MAX_ARTISTAS) {
                Toast.makeText(this, R.string.max_artists, Toast.LENGTH_SHORT).show();
                return;
            }
            artistNumberValue++;
            addButton.setEnabled(artistNumberValue < MAX_ARTISTAS);
            resButton.setEnabled(true);
            artistNumber.setText(getString(R.string.artist_info, addButton.getText(), artistNumberValue));
        });

        resButton.setOnClickListener(v -> {
            if (artistNumberValue == 1) {
                Toast.makeText(this, R.string.min_artists, Toast.LENGTH_SHORT).show();
                return;
            }
            artistNumberValue--;
            resButton.setEnabled(artistNumberValue > 1);
            addButton.setEnabled(true);
            artistNumber.setText(getString(R.string.artist_info, resButton.getText(), artistNumberValue));
        });

        // ===================== P2.2 Nombre de la canción =====================
        songName = findViewById(R.id.songName);
        songNameEdit = findViewById(R.id.songNameEdit);
        Button nameButton = findViewById(R.id.nameButton);

        nameButton.setOnClickListener(v -> {
            String texto = songNameEdit.getText().toString().trim();
            if (texto.isEmpty()) {
                Toast.makeText(this, R.string.write_a_name, Toast.LENGTH_SHORT).show();
                return;
            }
            nombreAceptado = texto;
            songName.setText(texto);
            songNameEdit.setText("");
        });

        // ===================== P2.3 ¿En mi biblioteca? =====================
        CheckBox checkBoxBiblioteca = findViewById(R.id.checkBoxBiblioteca);
        Button boxButton = findViewById(R.id.boxButton);
        TextView puss = findViewById(R.id.puss);

        // Relación inversa: casilla marcada -> botón apagado (y al revés).
        checkBoxBiblioteca.setOnCheckedChangeListener((v, isChecked) -> boxButton.setEnabled(!isChecked));
        boxButton.setOnClickListener(v -> puss.setText(R.string.button_pussed));

        // ===================== P4 Campos del formulario =====================
        generoSpinner = findViewById(R.id.generoSpinner);
        selloEdit = findViewById(R.id.selloEdit);
        minEdit = findViewById(R.id.minEdit);
        maxEdit = findViewById(R.id.maxEdit);
        yearEdit = findViewById(R.id.yearEdit);
        durationEdit = findViewById(R.id.durationEdit);
        popularityEdit = findViewById(R.id.popularityEdit);
        popularityError = findViewById(R.id.popularityError);

        // El Spinner se rellena desde el recurso XML res/values/arrays.xml
        ArrayAdapter<CharSequence> generos = ArrayAdapter.createFromResource(
                this, R.array.generos, android.R.layout.simple_spinner_item);
        generos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        generoSpinner.setAdapter(generos);

        // ===================== P3.2 Validación de la popularidad =====================
        popularityEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se usa
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se usa
            }

            @Override
            public void afterTextChanged(Editable s) {
                String texto = s.toString().trim();
                // Campo vacío: todavía no hay nada que validar -> sin aviso
                if (texto.isEmpty()) {
                    popularityError.setText("");
                    return;
                }
                popularityError.setText(esPopularidadValida(texto) ? "" : getString(R.string.popularity_error));
            }
        });

        // ===================== P3.1 Teclado de puntuación =====================
        scoreResult = findViewById(R.id.scoreResult);
        for (int i = 0; i < TECLAS.length; i++) {
            final int numero = i + 1;
            Button tecla = findViewById(TECLAS[i]);
            tecla.setOnClickListener(v -> scoreResult.setText(getString(R.string.score, numero)));
        }

        // ===================== P3.3 Estado de la canción =====================
        radioGroupState = findViewById(R.id.radioGroupState);
        Button clearButton = findViewById(R.id.clearButton);

        radioGroupState.setOnCheckedChangeListener((group, checkedId) -> {
            @ColorRes int color;
            if (checkedId == R.id.radioNew) {
                color = R.color.estado_nuevo;
            } else if (checkedId == R.id.radioListened) {
                color = R.color.estado_escuchado;
            } else if (checkedId == R.id.radioFavorite) {
                color = R.color.estado_favorito;
            } else {
                color = R.color.estado_neutro;
            }
            raiz.setBackgroundColor(ContextCompat.getColor(this, color));
        });

        clearButton.setOnClickListener(v -> {
            // clearCheck() desmarca de forma programática: NO dispara el listener
            // del grupo, así que el fondo hay que devolverlo a mano.
            radioGroupState.clearCheck();
            raiz.setBackgroundColor(ContextCompat.getColor(this, R.color.estado_neutro));
        });

        // ===================== P4 RESET y ENVIAR =====================
        Button resetFormButton = findViewById(R.id.resetFormButton);
        resetFormButton.setOnClickListener(v -> limpiarFormulario());

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> enviarFormulario());
    }

    /** Comprueba que el texto del campo de popularidad es un entero entre 1 y 10. */
    private boolean esPopularidadValida(String texto) {
        try {
            int popularidad = Integer.parseInt(texto);
            return popularidad >= 1 && popularidad <= 10;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Convierte un campo de texto en entero, o devuelve {@code -1} si no es un número. */
    private int leerEntero(EditText campo) {
        try {
            return Integer.parseInt(campo.getText().toString().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Nombre a enviar: lo que haya en el EditText o el último aceptado en la P2.2. */
    private String leerNombre() {
        String nombre = songNameEdit.getText().toString().trim();
        return nombre.isEmpty() ? nombreAceptado : nombre;
    }

    private void enviarFormulario() {
        String nombre = leerNombre();
        String genero = generoSpinner.getSelectedItemPosition() > 0
                ? generoSpinner.getSelectedItem().toString()
                : "";
        String sello = selloEdit.getText().toString().trim();
        int minimo = leerEntero(minEdit);
        int maximo = leerEntero(maxEdit);
        int anio = leerEntero(yearEdit);
        int duracion = leerEntero(durationEdit);
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        boolean popularidadOk = esPopularidadValida(popularityEdit.getText().toString().trim());

        boolean todoRelleno = !nombre.isEmpty() && !genero.isEmpty() && !sello.isEmpty()
                && minimo > 0 && maximo > 0 && anio > 0 && duracion > 0;
        boolean interpretesOk = todoRelleno && minimo <= maximo;
        boolean anioOk = interpretesOk && anio >= 1900 && anio <= anioActual;

        if (!todoRelleno || !interpretesOk || !anioOk || !popularidadOk) {
            Toast.makeText(this, R.string.wrong_data, Toast.LENGTH_SHORT).show();
            return;
        }

        int popularidad = Integer.parseInt(popularityEdit.getText().toString().trim());

        Intent intent = new Intent(this, FichaCancionActivity.class);
        intent.putExtra(EXTRA_NOMBRE, nombre);
        intent.putExtra(EXTRA_GENERO, genero);
        intent.putExtra(EXTRA_SELLO, sello);
        intent.putExtra(EXTRA_INTERPRETES_MIN, minimo);
        intent.putExtra(EXTRA_INTERPRETES_MAX, maximo);
        intent.putExtra(EXTRA_ANIO, anio);
        intent.putExtra(EXTRA_DURACION, duracion);
        intent.putExtra(EXTRA_POPULARIDAD, popularidad);
        startActivity(intent);
    }

    private void limpiarFormulario() {
        songNameEdit.setText("");
        songName.setText(R.string.name);
        nombreAceptado = "";

        generoSpinner.setSelection(0);
        selloEdit.setText("");
        minEdit.setText("");
        maxEdit.setText("");
        yearEdit.setText("");
        durationEdit.setText("");
        popularityEdit.setText("");
        popularityError.setText("");

        scoreResult.setText("");
        radioGroupState.clearCheck();
        raiz.setBackgroundColor(ContextCompat.getColor(this, R.color.estado_neutro));
    }
}
