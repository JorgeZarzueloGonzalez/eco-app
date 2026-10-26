package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.Serializable;

/**
 * Ficha de una canción (P4) ampliada en la P9: portada grande y valoración con
 * estrellas, además de género, sello, año y popularidad.
 * <p>
 * Sabe leer los datos de dos maneras: el objeto {@link Cancion} completo
 * (P9) o los extras sueltos del formulario de la P4.
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

        Cancion cancion = leerCancion(getIntent());

        ImageView portada = findViewById(R.id.fichaPortada);
        TextView fichaNombre = findViewById(R.id.fichaNombre);
        TextView fichaGenero = findViewById(R.id.fichaGenero);
        TextView fichaSello = findViewById(R.id.fichaSello);
        TextView fichaInterpretes = findViewById(R.id.fichaInterpretes);
        TextView fichaAnio = findViewById(R.id.fichaAnio);
        TextView fichaDuracion = findViewById(R.id.fichaDuracion);
        TextView fichaPopularidad = findViewById(R.id.fichaPopularidad);
        TextView fichaValoracion = findViewById(R.id.fichaValoracion);
        RatingBar fichaRatingBar = findViewById(R.id.fichaRatingBar);

        portada.setImageResource(Portadas.de(cancion.getGenero()));
        fichaNombre.setText(getString(R.string.ficha_nombre, cancion.getNombre()));
        fichaGenero.setText(getString(R.string.ficha_genero, cancion.getGenero()));
        fichaSello.setText(getString(R.string.ficha_sello, cancion.getSello()));
        fichaInterpretes.setText(getString(R.string.ficha_interpretes, cancion.getInterpretes()));
        fichaAnio.setText(getString(R.string.ficha_anio, cancion.getAnio()));
        fichaDuracion.setText(getString(R.string.ficha_duracion, cancion.getDuracion()));
        fichaPopularidad.setText(getString(R.string.ficha_popularidad, cancion.getPopularidad()));
        fichaValoracion.setText(getString(R.string.ficha_valoracion, cancion.getValoracion()));
        fichaRatingBar.setRating(cancion.getValoracion());
    }

    /**
     * Recoge la canción: si llega el objeto completo (P9) se usa tal cual; si no,
     * se reconstruye con los extras sueltos que manda el formulario de la P4.
     */
    private Cancion leerCancion(Intent intent) {
        Serializable extra = intent.getSerializableExtra(BibliotecaActivity.EXTRA_CANCION);
        if (extra instanceof Cancion) {
            return (Cancion) extra;
        }
        return new Cancion(
                texto(intent.getStringExtra(AltaCancionActivity.EXTRA_NOMBRE)),
                texto(intent.getStringExtra(AltaCancionActivity.EXTRA_GENERO)),
                texto(intent.getStringExtra(AltaCancionActivity.EXTRA_SELLO)),
                intent.getIntExtra(AltaCancionActivity.EXTRA_INTERPRETES, 0),
                intent.getIntExtra(AltaCancionActivity.EXTRA_ANIO, 0),
                intent.getIntExtra(AltaCancionActivity.EXTRA_POPULARIDAD, 0),
                intent.getIntExtra(AltaCancionActivity.EXTRA_DURACION, 0),
                intent.getIntExtra(AltaCancionActivity.EXTRA_VALORACION, 0));
    }

    /** Devuelve el texto recibido o el aviso "Sin datos" si no llegó nada. */
    private String texto(String valor) {
        return valor == null || valor.isEmpty() ? getString(R.string.sin_datos) : valor;
    }
}
