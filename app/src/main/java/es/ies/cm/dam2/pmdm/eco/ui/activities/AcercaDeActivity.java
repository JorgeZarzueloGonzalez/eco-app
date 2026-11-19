package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.util.Pantallas;

/**
 * Acerca de Eco: logo, versión, para qué sirve la app, autor y repositorio.
 * Se abre desde el menú de opciones de la biblioteca (Añadir, Ordenar,
 * Acerca de) y desde el cajón lateral.
 */
public class AcercaDeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acerca_de);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        Pantallas.barraConVuelta(this, findViewById(R.id.toolbar), R.string.acerca_de);

        // La versión se lee del propio APK (versionName de build.gradle.kts)
        TextView version = findViewById(R.id.acercaVersion);
        try {
            String nombre = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            version.setText(getString(R.string.acerca_version, nombre));
        } catch (PackageManager.NameNotFoundException e) {
            version.setText("");
        }
    }
}
