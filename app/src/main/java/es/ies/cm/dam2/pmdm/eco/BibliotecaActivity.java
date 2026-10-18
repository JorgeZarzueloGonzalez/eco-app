package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

/**
 * P8 La biblioteca de Eco: lista de canciones y alta de canciones nuevas.
 * <p>
 * Al abrir se ve el catálogo (géneros y canciones de la P7) con un adaptador de
 * fábrica; "Mostrar canciones" pinta la colección con el adaptador propio
 * (portada + nombre + intérpretes).
 */
public class BibliotecaActivity extends AppCompatActivity {

    /** Clave del extra con el objeto {@link Cancion} que devuelve el alta. */
    public static final String EXTRA_CANCION = "extra_cancion";

    /** El catálogo: los mismos géneros y canciones de la P7. */
    private static final String[][] CATALOGO = {
            {"Rock", "Luna de cobre"},
            {"Rock", "Tormenta"},
            {"Rock", "Camino al sur"},
            {"Pop", "Neón"},
            {"Pop", "Mariposa"},
            {"Pop", "Ciudad dormida"},
            {"Electrónica", "Pulso"},
            {"Electrónica", "Circuito"},
            {"Electrónica", "Amanecer binario"}
    };

    /** LA COLECCIÓN: atributo de clase, la biblioteca de la app. */
    private final ArrayList<Cancion> coleccion = new ArrayList<>();

    /** true cuando la lista está mostrando la colección (y no el catálogo). */
    private boolean mostrandoColeccion = false;

    private ListView bibliotecaList;

    /** El lanzador se registra como atributo: recibe la canción del alta. */
    private final ActivityResultLauncher<Intent> lanzadorAlta =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), respuesta -> {
                if (respuesta.getResultCode() == RESULT_OK && respuesta.getData() != null) {
                    // Los objetos viajan como Serializable.
                    Cancion nueva = (Cancion) respuesta.getData().getSerializableExtra(EXTRA_CANCION);
                    if (nueva != null) {
                        coleccion.add(nueva);
                        Toast.makeText(this, getString(R.string.biblioteca_anadida, nueva.getNombre()),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_biblioteca);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bibliotecaList = findViewById(R.id.bibliotecaList);

        // Primera versión de la lista: el catálogo, con adaptador de fábrica.
        ArrayList<String> textosCatalogo = new ArrayList<>();
        for (String[] fila : CATALOGO) {
            textosCatalogo.add(getString(R.string.catalogo_toast, fila[0], fila[1]));
        }
        ArrayAdapter<String> adaptadorCatalogo = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, textosCatalogo);
        bibliotecaList.setAdapter(adaptadorCatalogo);

        // Clic en una fila: Toast con lo que hay en esa posición.
        bibliotecaList.setOnItemClickListener((parent, view, position, id) -> {
            if (mostrandoColeccion) {
                Toast.makeText(this, coleccion.get(position).toString(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,
                        getString(R.string.catalogo_toast, CATALOGO[position][0], CATALOGO[position][1]),
                        Toast.LENGTH_SHORT).show();
            }
        });

        Button anadirButton = findViewById(R.id.anadirButton);
        anadirButton.setOnClickListener(v ->
                // Se lanza CON EL LANZADOR para recibir la canción de vuelta.
                lanzadorAlta.launch(new Intent(this, AltaCancionActivity.class)));

        Button mostrarButton = findViewById(R.id.mostrarButton);
        mostrarButton.setOnClickListener(v -> mostrarColeccion());
    }

    /** Pinta la colección con el adaptador propio (portadas y tonos alternos). */
    private void mostrarColeccion() {
        if (coleccion.isEmpty()) {
            Toast.makeText(this, R.string.biblioteca_vacia, Toast.LENGTH_SHORT).show();
        }
        bibliotecaList.setAdapter(new AdaptadorCancion(this, coleccion));
        mostrandoColeccion = true;
    }
}
