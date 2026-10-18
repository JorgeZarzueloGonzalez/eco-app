package es.ies.cm.dam2.pmdm.eco;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * P7 Controles de selección: elegir una canción por género, título y ubicación.
 * <p>
 * Dos {@link Spinner} (género y canción) + un {@link ListView} de ubicaciones con
 * RadioButton (plantilla de Android) + botón Mostrar, que solo se activa cuando
 * están las tres selecciones.
 */
public class BuscarActivity extends AppCompatActivity {

    /** Los géneros (arrays de Java, como pide el enunciado). */
    private static final String[] GENEROS = {"Rock", "Pop", "Electrónica"};

    /** Las canciones de cada género: la fila i corresponde a GENEROS[i]. */
    private static final String[][] CANCIONES = {
            {"Luna de cobre", "Tormenta", "Camino al sur"},
            {"Neón", "Mariposa", "Ciudad dormida"},
            {"Pulso", "Circuito", "Amanecer binario"}
    };

    private Spinner generoSpinner;
    private Spinner cancionSpinner;
    private ListView ubicacionesList;
    private Button mostrarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buscar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        generoSpinner = findViewById(R.id.generoBuscarSpinner);
        cancionSpinner = findViewById(R.id.cancionBuscarSpinner);
        ubicacionesList = findViewById(R.id.ubicacionesList);
        mostrarButton = findViewById(R.id.mostrarButton);

        // Cada Spinner usa su propia plantilla de fila: así cada uno tiene su color.
        ArrayAdapter<String> adaptadorGeneros = new ArrayAdapter<>(
                this, R.layout.item_spinner_genero, GENEROS);
        adaptadorGeneros.setDropDownViewResource(R.layout.item_spinner_genero);
        generoSpinner.setAdapter(adaptadorGeneros);

        // El ListView usa la plantilla con RadioButton que trae Android.
        ArrayAdapter<CharSequence> adaptadorUbicaciones = ArrayAdapter.createFromResource(
                this, R.array.ubicaciones, android.R.layout.simple_list_item_single_choice);
        ubicacionesList.setAdapter(adaptadorUbicaciones);

        // Listener de SELECCIÓN (no de pulsación): al cambiar el género, se filtran las canciones.
        generoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarCanciones(position);
                actualizarBoton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se usa
            }
        });

        cancionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarBoton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se usa
            }
        });

        ubicacionesList.setOnItemClickListener((parent, view, position, id) -> actualizarBoton());

        mostrarButton.setOnClickListener(v -> mostrarCaracteristicas());

        // Al arrancar ya hay un género elegido: se cargan sus canciones.
        actualizarCanciones(generoSpinner.getSelectedItemPosition());
        actualizarBoton();
    }

    /** Rellena el Spinner de canciones con las del género elegido (filtrado dependiente). */
    private void actualizarCanciones(int posicionGenero) {
        if (posicionGenero < 0 || posicionGenero >= CANCIONES.length) {
            return;
        }
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this, R.layout.item_spinner_cancion, CANCIONES[posicionGenero]);
        adaptador.setDropDownViewResource(R.layout.item_spinner_cancion);
        cancionSpinner.setAdapter(adaptador);
    }

    /** El botón solo se enciende con las TRES selecciones hechas. */
    private void actualizarBoton() {
        boolean hayGenero = generoSpinner.getSelectedItemPosition() != AdapterView.INVALID_POSITION;
        boolean hayCancion = cancionSpinner.getSelectedItemPosition() != AdapterView.INVALID_POSITION;
        boolean hayUbicacion = ubicacionesList.getCheckedItemPosition() != AdapterView.INVALID_POSITION;
        mostrarButton.setEnabled(hayGenero && hayCancion && hayUbicacion);
    }

    private void mostrarCaracteristicas() {
        int posicionUbicacion = ubicacionesList.getCheckedItemPosition();
        if (posicionUbicacion == AdapterView.INVALID_POSITION) {
            return;
        }
        String genero = String.valueOf(generoSpinner.getSelectedItem());
        String cancion = String.valueOf(cancionSpinner.getSelectedItem());
        String ubicacion = String.valueOf(ubicacionesList.getItemAtPosition(posicionUbicacion));
        Toast.makeText(this, getString(R.string.buscar_toast, genero, cancion, ubicacion),
                Toast.LENGTH_LONG).show();
    }
}
