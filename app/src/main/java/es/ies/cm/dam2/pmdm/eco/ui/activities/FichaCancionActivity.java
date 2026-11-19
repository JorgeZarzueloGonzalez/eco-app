package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.data.room.AppDatabase;
import es.ies.cm.dam2.pmdm.eco.data.room.CancionDao;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import es.ies.cm.dam2.pmdm.eco.ui.dialogs.ConfirmarDialogFragment;
import es.ies.cm.dam2.pmdm.eco.util.Formatos;
import es.ies.cm.dam2.pmdm.eco.util.Portadas;

/**
 * Ficha (detalle) de una canción.
 * <p>
 * Recibe la canción por el Intent, pero en {@code onResume} la <b>vuelve a leer
 * de Room por su id</b>: así, al volver de Editar, enseña los datos nuevos y no
 * los que trajo el Intent (no quedan datos obsoletos).
 * <ul>
 *     <li>Menú de opciones propio: <b>Editar</b> y <b>Eliminar</b>.</li>
 *     <li>Switch <b>Favorita</b>: guarda en Room en cuanto se toca.</li>
 *     <li><b>Ver reproducciones</b>: abre las reproducciones de esta canción.</li>
 * </ul>
 */
public class FichaCancionActivity extends AppCompatActivity {

    /** Clave del diálogo de confirmación de borrado. */
    private static final String BORRAR = "ficha_borrar";

    private CancionDao cancionDao;
    private Cancion cancion;

    private ImageView portada;
    private TextView nombre, genero, estado, explicita, valoracion;
    private TextView sello, interpretes, anio, duracion, popularidad, reproducciones;
    private RatingBar ratingBar;
    private MaterialSwitch favoritaSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ficha_cancion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        cancion = (Cancion) getIntent().getSerializableExtra(BibliotecaActivity.EXTRA_CANCION);
        if (cancion == null) {
            finish();
            return;
        }
        cancionDao = AppDatabase.getInstancia(this).cancionDao();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.ficha_title);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        enlazarVistas();

        // El Switch guarda al momento. Solo reacciona si lo toca el usuario
        // (isPressed), no cuando lo cambia el código al pintar la ficha.
        favoritaSwitch.setOnCheckedChangeListener((boton, marcado) -> {
            if (boton.isPressed() && cancion.isFavorita() != marcado) {
                cancion.setFavorita(marcado);
                Cancion aGuardar = cancion;
                AppDatabase.executor.execute(() -> cancionDao.actualizar(aGuardar));
            }
        });

        findViewById(R.id.fichaVerReproducciones).setOnClickListener(v -> {
            Intent intent = new Intent(this, ReproduccionActivity.class);
            intent.putExtra(ReproduccionActivity.EXTRA_CANCION_ID, cancion.getId());
            startActivity(intent);
        });

        // Respuesta del diálogo de borrar (también llega bien después de girar)
        getSupportFragmentManager().setFragmentResultListener(BORRAR, this,
                (clave, resultado) -> borrar());
    }

    /** Se vuelve a leer la canción de Room cada vez que la pantalla vuelve a verse. */
    @Override
    protected void onResume() {
        super.onResume();
        int id = cancion.getId();
        AppDatabase.executor.execute(() -> {
            Cancion actual = cancionDao.porId(id);
            int total = AppDatabase.getInstancia(this).reproduccionDao().contar(id);
            runOnUiThread(() -> {
                if (actual == null) {      // la borraron desde otra pantalla
                    finish();
                    return;
                }
                cancion = actual;
                pintar(total);
            });
        });
    }

    private void enlazarVistas() {
        portada = findViewById(R.id.fichaPortada);
        nombre = findViewById(R.id.fichaNombre);
        genero = findViewById(R.id.fichaGenero);
        estado = findViewById(R.id.fichaEstado);
        explicita = findViewById(R.id.fichaExplicita);
        valoracion = findViewById(R.id.fichaValoracion);
        ratingBar = findViewById(R.id.fichaRatingBar);
        favoritaSwitch = findViewById(R.id.fichaFavorita);
        sello = findViewById(R.id.fichaSello);
        interpretes = findViewById(R.id.fichaInterpretes);
        anio = findViewById(R.id.fichaAnio);
        duracion = findViewById(R.id.fichaDuracion);
        popularidad = findViewById(R.id.fichaPopularidad);
        reproducciones = findViewById(R.id.fichaReproducciones);
    }

    private void pintar(int totalReproducciones) {
        portada.setImageResource(Portadas.de(cancion.getGenero()));
        nombre.setText(cancion.getNombre());
        genero.setText(cancion.getGenero());

        estado.setText(Formatos.estado(this, cancion.getEstado()));
        estado.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, Formatos.colorEstado(cancion.getEstado()))));
        explicita.setVisibility(cancion.isExplicita() ? View.VISIBLE : View.GONE);

        ratingBar.setRating(cancion.getValoracion());
        valoracion.setText(getString(R.string.ficha_valoracion, cancion.getValoracion()));
        favoritaSwitch.setChecked(cancion.isFavorita());

        sello.setText(cancion.getSello());
        interpretes.setText(String.valueOf(cancion.getInterpretes()));
        anio.setText(String.valueOf(cancion.getAnio()));
        duracion.setText(Formatos.duracion(cancion.getDuracion()));
        popularidad.setText(getString(R.string.ficha_popularidad_valor, cancion.getPopularidad()));
        reproducciones.setText(String.valueOf(totalReproducciones));
    }

    // ===================== menú de opciones =====================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ficha, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.fichaEditar) {
            Intent intent = new Intent(this, AltaCancionActivity.class);
            intent.putExtra(AltaCancionActivity.EXTRA_CANCION, cancion);
            startActivity(intent);
            return true;
        }
        if (id == R.id.fichaEliminar) {
            ConfirmarDialogFragment.mostrar(getSupportFragmentManager(), BORRAR,
                    getString(R.string.borrar_cancion_titulo),
                    getString(R.string.borrar_cancion_pregunta, cancion.getNombre()),
                    cancion.getId());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Borra la canción (y en cascada sus reproducciones) y vuelve a la lista. */
    private void borrar() {
        Cancion aBorrar = cancion;
        AppDatabase.executor.execute(() -> {
            cancionDao.borrar(aBorrar);
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.cancion_borrada, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
