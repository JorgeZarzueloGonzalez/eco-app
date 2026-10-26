package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * P8/P9 La biblioteca de Eco: la colección de canciones.
 * <p>
 * En la P8 se pintaba con un {@code ListView}; en la P9 pasa a un
 * {@link RecyclerView} con tarjetas. La colección se carga estáticamente en él
 * {@code onCreate} (más de 20 canciones) y el botón Añadir mete las que devuelve
 * el formulario de alta.
 */
public class BibliotecaActivity extends AppCompatActivity
        implements AdaptadorCancionRecycler.OyenteCancion {

    /**
     * Clave del extra con el objeto {@link Cancion}: lo usan el alta (devuelve) y la ficha (recibe).
     */
    public static final String EXTRA_CANCION = "extra_cancion";

    /**
     * LA COLECCIÓN: atributo de clase, la biblioteca de la app.
     */
    private final ArrayList<Cancion> coleccion = new ArrayList<>();

    private AdaptadorCancionRecycler adaptador;
    private LinearLayoutManager disposicion;

    /**
     * El lanzador se registra como atributo: recibe la canción del alta (P8).
     */
    private final ActivityResultLauncher<Intent> lanzadorAlta =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), respuesta -> {
                if (respuesta.getResultCode() == RESULT_OK && respuesta.getData() != null) {
                    // Los objetos viajan como Serializable.
                    Cancion nueva = (Cancion) respuesta.getData().getSerializableExtra(EXTRA_CANCION);
                    if (nueva != null) {
                        coleccion.add(nueva);
                        adaptador.notifyItemInserted(coleccion.size() - 1);
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

        // 1. La colección (carga estática: mínimo 20 canciones)
        cargarColeccion();

        // 2. El RecyclerView con su LayoutManager (sin él no se pinta NADA)
        RecyclerView bibliotecaRecycler = findViewById(R.id.bibliotecaRecycler);
        disposicion = new LinearLayoutManager(this);   // una sola columna
        bibliotecaRecycler.setLayoutManager(disposicion);

        // 3. El adaptador (this = esta activity escucha los clics)
        adaptador = new AdaptadorCancionRecycler(coleccion, this);
        bibliotecaRecycler.setAdapter(adaptador);

        // 4. Los botones de siempre
        Button anadirButton = findViewById(R.id.anadirButton);
        anadirButton.setOnClickListener(v ->
                // Se lanza CON EL LANZADOR para recibir la canción de vuelta.
                lanzadorAlta.launch(new Intent(this, AltaCancionActivity.class)));

        Button mostrarButton = findViewById(R.id.mostrarButton);
        mostrarButton.setOnClickListener(v -> mostrarColeccion());
    }

    /**
     * P9: la colección se rellena a mano aquí, sin base de datos ni ficheros.
     */
    private void cargarColeccion() {
        coleccion.add(new Cancion("Luna de cobre", "Rock", "Eco Records", 2, 2024, 8, 180, 7));
        coleccion.add(new Cancion("Tormenta", "Rock", "Eco Records", 3, 2021, 7, 205, 6));
        coleccion.add(new Cancion("Camino al sur", "Rock", "Sur Records", 1, 2019, 6, 165, 5));
        coleccion.add(new Cancion("Viento norte", "Rock", "Sur Records", 4, 2023, 9, 190, 8));
        coleccion.add(new Cancion("Semilla", "Rock", "Eco Records", 2, 2020, 5, 150, 4));
        coleccion.add(new Cancion("Fuego lento", "Rock", "Sur Records", 2, 2022, 8, 175, 7));
        coleccion.add(new Cancion("Arena", "Rock", "Eco Records", 3, 2018, 6, 200, 6));

        coleccion.add(new Cancion("Neón", "Pop", "Luces Records", 1, 2025, 9, 210, 9));
        coleccion.add(new Cancion("Mariposa", "Pop", "Luces Records", 1, 2023, 8, 195, 7));
        coleccion.add(new Cancion("Ciudad dormida", "Pop", "Luces Records", 2, 2022, 7, 185, 6));
        coleccion.add(new Cancion("Azúcar", "Pop", "Sol Records", 1, 2024, 9, 200, 8));
        coleccion.add(new Cancion("Ventana", "Pop", "Sol Records", 2, 2021, 6, 170, 5));
        coleccion.add(new Cancion("Domingo", "Pop", "Luces Records", 1, 2020, 7, 160, 6));
        coleccion.add(new Cancion("Papel de plata", "Pop", "Sol Records", 4, 2019, 5, 155, 4));

        coleccion.add(new Cancion("Pulso", "Electrónica", "Circuito Records", 1, 2025, 9, 240, 9));
        coleccion.add(new Cancion("Circuito", "Electrónica", "Circuito Records", 2, 2024, 8, 225, 8));
        coleccion.add(new Cancion("Amanecer binario", "Electrónica", "Nébula Records", 3, 2023, 7, 210, 7));
        coleccion.add(new Cancion("Órbita", "Electrónica", "Nébula Records", 2, 2022, 8, 235, 8));
        coleccion.add(new Cancion("Señal", "Electrónica", "Circuito Records", 1, 2021, 6, 205, 5));
        coleccion.add(new Cancion("Bitácora", "Electrónica", "Nébula Records", 3, 2020, 7, 220, 6));
        coleccion.add(new Cancion("Nébula", "Electrónica", "Nébula Records", 1, 2019, 6, 215, 5));
    }

    /**
     * El botón Mostrar vuelve al principio de la lista y la repinta.
     */
    private void mostrarColeccion() {
        if (coleccion.isEmpty()) {
            Toast.makeText(this, R.string.biblioteca_vacia, Toast.LENGTH_SHORT).show();
            return;
        }
        adaptador.notifyDataSetChanged();
        disposicion.scrollToPosition(0);
    }

    /**
     * Clic corto: se abre la ficha con la canción completa (objeto Serializable).
     */
    @Override
    public void alHacerClicCorto(int posicion) {
        Cancion cancion = coleccion.get(posicion);
        Intent intent = new Intent(this, FichaCancionActivity.class);
        intent.putExtra(EXTRA_CANCION, cancion);
        startActivity(intent);
    }

    /**
     * Clic largo: se borra la canción de la colección y se avisa.
     */
    @Override
    public void alHacerClicLargo(int posicion) {
        coleccion.remove(posicion);
        adaptador.notifyItemRemoved(posicion);
        Toast.makeText(this, R.string.cancion_borrada, Toast.LENGTH_SHORT).show();
    }
}
