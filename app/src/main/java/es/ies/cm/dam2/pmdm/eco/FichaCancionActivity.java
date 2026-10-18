package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Ficha de una canción: recibe los datos por extras del Intent y los pinta.
 * <p>
 * Es la pantalla que se reutilizará en prácticas siguientes (P9): por eso lee
 * los datos de forma genérica y con claves constantes.
 */
public class FichaCancionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ficha_cancion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String nombre = intent.getStringExtra(AltaCancionActivity.EXTRA_NOMBRE);
        String genero = intent.getStringExtra(AltaCancionActivity.EXTRA_GENERO);
        String sello = intent.getStringExtra(AltaCancionActivity.EXTRA_SELLO);
        int interpretesMin = intent.getIntExtra(AltaCancionActivity.EXTRA_INTERPRETES_MIN, 0);
        int interpretesMax = intent.getIntExtra(AltaCancionActivity.EXTRA_INTERPRETES_MAX, 0);
        int anio = intent.getIntExtra(AltaCancionActivity.EXTRA_ANIO, 0);
        int duracion = intent.getIntExtra(AltaCancionActivity.EXTRA_DURACION, 0);
        int popularidad = intent.getIntExtra(AltaCancionActivity.EXTRA_POPULARIDAD, 0);

        TextView fichaNombre = findViewById(R.id.fichaNombre);
        TextView fichaGenero = findViewById(R.id.fichaGenero);
        TextView fichaSello = findViewById(R.id.fichaSello);
        TextView fichaInterpretes = findViewById(R.id.fichaInterpretes);
        TextView fichaAnio = findViewById(R.id.fichaAnio);
        TextView fichaDuracion = findViewById(R.id.fichaDuracion);
        TextView fichaPopularidad = findViewById(R.id.fichaPopularidad);

        fichaNombre.setText(nombre == null ? getString(R.string.ficha_title)
                : getString(R.string.ficha_nombre, nombre));
        fichaGenero.setText(getString(R.string.ficha_genero, texto(genero)));
        fichaSello.setText(getString(R.string.ficha_sello, texto(sello)));
        fichaInterpretes.setText(getString(R.string.ficha_interpretes, interpretesMin, interpretesMax));
        fichaAnio.setText(getString(R.string.ficha_anio, anio));
        fichaDuracion.setText(getString(R.string.ficha_duracion, duracion));
        fichaPopularidad.setText(getString(R.string.ficha_popularidad, popularidad));
    }

    /** Devuelve el texto recibido o el aviso "Sin datos" si no llegó nada. */
    private String texto(String valor) {
        return valor == null || valor.isEmpty() ? getString(R.string.sin_datos) : valor;
    }
}
