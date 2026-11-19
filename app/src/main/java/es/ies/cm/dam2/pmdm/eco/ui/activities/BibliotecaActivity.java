package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.util.Pantallas;
import es.ies.cm.dam2.pmdm.eco.data.room.AppDatabase;
import es.ies.cm.dam2.pmdm.eco.data.room.CancionDao;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import es.ies.cm.dam2.pmdm.eco.ui.adapters.AdaptadorCancionRecycler;
import es.ies.cm.dam2.pmdm.eco.ui.dialogs.ConfirmarDialogFragment;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * P8/P9 La biblioteca de Eco: la colección de canciones.
 * <p>
 * En la P8 se pintaba con un {@code ListView}; en la P9 pasa a un
 * {@link RecyclerView} con tarjetas. Desde la P12 la lista se recarga en
 * {@code onResume} y el menú contextual edita o borra la fila.
 * <p>
 * P13: las canciones están en Room ({@code eco_room.db}). Cada acceso al
 * {@link CancionDao} va en {@link AppDatabase#executor} y el adaptador se
 * actualiza de vuelta en el hilo principal con {@code runOnUiThread}.
 */
public class BibliotecaActivity extends AppCompatActivity
        implements AdaptadorCancionRecycler.OyenteCancion {

    /**
     * Clave del extra con el objeto {@link Cancion}: lo usan el alta (devuelve) y la ficha (recibe).
     */
    public static final String EXTRA_CANCION = "extra_cancion";

    /** Clave del diálogo de confirmación de borrado. */
    private static final String BORRAR = "biblioteca_borrar";

    /**
     * LA COLECCIÓN: atributo de clase, la biblioteca de la app.
     */
    private final ArrayList<Cancion> coleccion = new ArrayList<>();

    private AdaptadorCancionRecycler adaptador;
    private LinearLayoutManager disposicion;
    private Cancion cancionSeleccionada;
    private CancionDao cancionDao;

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
        toolbar.setTitleTextColor(getResources().getColor(R.color.accent, getTheme()));
        Pantallas.barraConVuelta(this, toolbar, R.string.biblioteca_titulo);

        cancionDao = AppDatabase.getInstancia(this).cancionDao();

        // 2. El RecyclerView con su LayoutManager (sin él no se pinta NADA)
        RecyclerView bibliotecaRecycler = findViewById(R.id.bibliotecaRecycler);
        disposicion = new LinearLayoutManager(this);   // una sola columna
        bibliotecaRecycler.setLayoutManager(disposicion);

        // 3. El adaptador (this = esta activity escucha los clics)
        adaptador = new AdaptadorCancionRecycler(coleccion, this);
        bibliotecaRecycler.setAdapter(adaptador);

        // Respuesta del diálogo de borrar
        getSupportFragmentManager().setFragmentResultListener(BORRAR, this,
                (clave, resultado) -> borrar(resultado.getInt(ConfirmarDialogFragment.RESULTADO_ID)));
    }

    /**
     * Consulta Room en segundo plano y repinta la lista en el hilo principal.
     * Las vistas solo se pueden tocar desde el hilo principal.
     */
    private void cargarCanciones() {
        AppDatabase.executor.execute(() -> {
            List<Cancion> canciones = cancionDao.todas();
            runOnUiThread(() -> {
                coleccion.clear();
                coleccion.addAll(canciones);
                adaptador.notifyDataSetChanged();
            });
        });
    }

    /**
     * Al volver del alta, de la ficha o de la edición se recarga desde la
     * base de datos. Así la BD manda y no hay canciones duplicadas en memoria.
     * (También es la primera carga: onResume se ejecuta justo después de onCreate.)
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarCanciones();
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
            startActivity(intent);
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
     * Clic largo: abre el menú contextual de la canción (Editar / Eliminar).
     */
    @Override
    public void alHacerClicLargo(int posicion, View itemview) {
        if (posicion == RecyclerView.NO_POSITION) return;

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

    /** Pregunta con el DialogFragment propio; la respuesta llega a {@link #borrar(int)}. */
    private void confirmarBorrado() {
        ConfirmarDialogFragment.mostrar(getSupportFragmentManager(), BORRAR,
                getString(R.string.borrar_cancion_titulo),
                getString(R.string.borrar_cancion_pregunta, cancionSeleccionada.getNombre()),
                cancionSeleccionada.getId());
    }

    /**
     * Borra por {@code id}; Room borra en cascada sus reproducciones. El id llega
     * en la respuesta del diálogo, así que funciona aunque se haya girado el móvil.
     */
    private void borrar(int id) {
        AppDatabase.executor.execute(() -> {
            Cancion aBorrar = cancionDao.porId(id);
            if (aBorrar != null) {
                cancionDao.borrar(aBorrar);
            }
            runOnUiThread(() ->
                    Toast.makeText(this, R.string.cancion_borrada, Toast.LENGTH_SHORT).show());
        });
        cargarCanciones(); // se encola detrás del borrado: el executor tiene un solo hilo
    }

    private void abrirEdicion() {
        Intent intent = new Intent(this, AltaCancionActivity.class);
        intent.putExtra(AltaCancionActivity.EXTRA_CANCION, cancionSeleccionada);
        startActivity(intent);
    }
}
