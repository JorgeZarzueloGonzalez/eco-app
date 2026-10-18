package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Pantalla de alta de una canción.
 * <p>
 * P2: contadores de intérpretes, nombre de la canción y "¿en mi biblioteca?".
 * P3: validación de la popularidad en rojo y estado de la canción con color de fondo.
 */
public class AltaCancionActivity extends AppCompatActivity {

    /** Máximo de intérpretes que permite la pantalla. */
    private static final int MAX_ARTISTAS = 4;

    /** Número de intérpretes actual (atributo de clase: lo usan varias lambdas). */
    private int artistNumberValue = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alta_cancion);

        final View raiz = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ================= P2.1 Contadores de intérpretes =================
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

        // ================= P2.2 Nombre de la canción =================
        TextView songName = findViewById(R.id.songName);
        EditText songNameEdit = findViewById(R.id.songNameEdit);
        Button nameButton = findViewById(R.id.nameButton);

        nameButton.setOnClickListener(v -> {
            String texto = songNameEdit.getText().toString().trim();
            if (texto.isEmpty()) {
                Toast.makeText(this, R.string.write_a_name, Toast.LENGTH_SHORT).show();
                return;
            }
            songName.setText(texto);
            songNameEdit.setText("");
        });

        // ================= P2.3 ¿En mi biblioteca? =================
        CheckBox checkBoxBiblioteca = findViewById(R.id.checkBoxBiblioteca);
        Button boxButton = findViewById(R.id.boxButton);
        TextView puss = findViewById(R.id.puss);

        // Relación inversa: casilla marcada -> botón apagado (y al revés).
        checkBoxBiblioteca.setOnCheckedChangeListener((v, isChecked) -> boxButton.setEnabled(!isChecked));
        boxButton.setOnClickListener(v -> puss.setText(R.string.button_pussed));

        // ================= P3.2 Validación de la popularidad =================
        EditText popularityEdit = findViewById(R.id.popularityEdit);
        TextView popularityError = findViewById(R.id.popularityError);

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
                try {
                    int popularidad = Integer.parseInt(texto);
                    if (popularidad >= 1 && popularidad <= 10) {
                        popularityError.setText("");
                    } else {
                        popularityError.setText(R.string.popularity_error);
                    }
                } catch (NumberFormatException e) {
                    // Se han colado letras o algo que no es un número
                    popularityError.setText(R.string.popularity_error);
                }
            }
        });

        // ================= P3.3 Estado de la canción =================
        RadioGroup radioGroupState = findViewById(R.id.radioGroupState);
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

        // ================= Navegación al teclado de puntuación (P3.1) =================
        Button nextActivityButton = findViewById(R.id.nextActivityButton);
        nextActivityButton.setOnClickListener(v ->
                startActivity(new Intent(this, AltaCancion2Activity.class)));
    }
}
