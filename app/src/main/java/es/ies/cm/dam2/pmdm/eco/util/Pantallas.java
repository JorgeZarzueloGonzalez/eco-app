package es.ies.cm.dam2.pmdm.eco.util;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

/** Ayudas comunes a varias pantallas. */
public final class Pantallas {

    private Pantallas() {
        // Clase de utilidades: no se instancia
    }

    /**
     * Pone la toolbar como barra de la pantalla con la flecha "atrás", que
     * cierra la activity (cada ida tiene su vuelta).
     *
     * @param titulo id del texto del título, o 0 para no enseñar título
     */
    public static void barraConVuelta(AppCompatActivity activity, MaterialToolbar toolbar, int titulo) {
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowTitleEnabled(titulo != 0);
        }
        if (titulo != 0) {
            // El título se pone a la activity: AppCompat lo copia a la barra (si se
            // pusiera solo en la toolbar, lo pisaría el nombre de la app).
            activity.setTitle(titulo);
        }
        toolbar.setNavigationOnClickListener(v -> activity.finish());
    }
}
