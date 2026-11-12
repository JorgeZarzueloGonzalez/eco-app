package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;

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
    private int posicionEditando;
    private Cancion cancionSeleccionada;
    private CancionRepository cancionRepository;

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

    private final ActivityResultLauncher<Intent> launcherEdicion = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
        if (result.getResultCode() == RESULT_OK) {
            Cancion editada = (Cancion) result.getData()
                    .getSerializableExtra(AltaCancionActivity.EXTRA_CANCION);

            coleccion.set(posicionEditando, editada);       // sustituye
            adaptador.notifyItemChanged(posicionEditando);
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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.biblioteca_titulo);
        toolbar.setTitleTextColor(getResources().getColor(R.color.accent, getTheme()));

        setSupportActionBar(toolbar);

        cancionRepository = new CancionRepository(this);

        cargarCanciones();

        // 2. El RecyclerView con su LayoutManager (sin él no se pinta NADA)
        RecyclerView bibliotecaRecycler = findViewById(R.id.bibliotecaRecycler);
        disposicion = new LinearLayoutManager(this);   // una sola columna
        bibliotecaRecycler.setLayoutManager(disposicion);

        // 3. El adaptador (this = esta activity escucha los clics)
        adaptador = new AdaptadorCancionRecycler(coleccion, this);
        bibliotecaRecycler.setAdapter(adaptador);
    }

    private void cargarCanciones() {
        coleccion.clear();
        coleccion.addAll(cancionRepository.getCanciones());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_biblioteca, menu);
        return true; // obligatorio
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.accion_anadir) {
            Intent intent = new Intent(this, AltaCancionActivity.class);
            intent.putExtra("extra_to_biblioteca", true);
            lanzadorAlta.launch(intent);
            return true;
        }
        if (id == R.id.accion_ordenar) {
            ordenarCanciones();
            return true;
        }
        if (id == R.id.accion_acerca_de) {
            Intent intent = new Intent(this, AcercaDeActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ordenarCanciones() {
        Collator comparador = Collator.getInstance(new Locale("es", "ES"));
        coleccion.sort((c1, c2) -> comparador.compare(c1.getNombre(), c2.getNombre()));
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
    public void alHacerClicLargo(int posicion, View itemview) {
        if (posicion == RecyclerView.NO_POSITION) return;

        posicionEditando = posicion;
        cancionSeleccionada = coleccion.get(posicion);
        mostarContextMenu(itemview);
    }

    public void mostarContextMenu(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.menu_contextual_cancion, menu.getMenu());

        menu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.cancionEditar) {
                abrirEdicion();
                return true;
            }
            if (id == R.id.cancionEliminar) {
                confirmarBorrado();
                return true;
            }
            return false;
        });

        menu.show();
    }

    private void confirmarBorrado() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar canción")
                .setMessage("¿Eliminar «" + cancionSeleccionada.getNombre() + "» de tu biblioteca?")
                .setPositiveButton("Eliminar", (dialog, which) -> borrarSeleccionada())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void borrarSeleccionada() {
        coleccion.remove(posicionEditando);
        cancionRepository.deleteCancion(cancionSeleccionada.getId());
        adaptador.notifyItemRemoved(posicionEditando);
        Toast.makeText(this, "Canción eliminada", Toast.LENGTH_SHORT).show();
    }

    private void abrirEdicion() {
        Intent intent = new Intent(this, AltaCancionActivity.class);
        intent.putExtra(String.valueOf(AltaCancionActivity.EXTRA_CANCION), cancionSeleccionada);
        launcherEdicion.launch(intent);
    }
}
