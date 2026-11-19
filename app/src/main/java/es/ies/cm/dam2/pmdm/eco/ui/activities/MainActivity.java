package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import es.ies.cm.dam2.pmdm.eco.R;

/**
 * Pantalla de inicio de Eco.
 * <p>
 * Tiene dos formas de moverse por la app:
 * <ul>
 *     <li>Los <b>cuatro accesos</b> principales de la portada: biblioteca,
 *     buscar, añadir canción y mis playlists.</li>
 *     <li>El <b>cajón lateral</b> (Navigation Drawer, P11), con esos mismos
 *     destinos y el resto de pantallas.</li>
 * </ul>
 * Las playlists son personales: se entra por el login.
 */
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Con EdgeToEdge la ventana también ocupa la barra de estado y el notch,
        // así que hay que apartar a mano el contenido de esa zona.
        // (El cajón lo hace él solo con android:fitsSystemWindows="true" en el XML.)
        aplicarInsets(R.id.contenido);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // el nombre ya sale grande
        }

        // ===================== Cajón lateral =====================
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // El "botón hamburguesa" de la toolbar que abre y cierra el cajón
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Con el cajón abierto, Atrás lo cierra en vez de salir de la app.
        OnBackPressedCallback cerrarCajon = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                drawerLayout.close();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, cerrarCajon);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View cajon) {
                cerrarCajon.setEnabled(true);
            }

            @Override
            public void onDrawerClosed(View cajon) {
                cerrarCajon.setEnabled(false);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            Class<?> destino = destinoDelCajon(item.getItemId());
            drawerLayout.close();
            if (destino == null) {
                return false;
            }
            abrir(destino);
            return true;
        });

        // ===================== Accesos de la portada =====================
        findViewById(R.id.bibliotecaButton).setOnClickListener(v -> abrir(BibliotecaActivity.class));
        findViewById(R.id.buscarButton).setOnClickListener(v -> abrir(BuscarActivity.class));
        findViewById(R.id.altaButton).setOnClickListener(v -> abrir(AltaCancionActivity.class));
        findViewById(R.id.playlistsButton).setOnClickListener(v -> abrir(LoginActivity.class));

        // El pie "Creado por…" abre la paleta de colores de la app (P1b)
        findViewById(R.id.Created).setOnClickListener(v -> abrir(PaletaActivity.class));
    }

    /** Qué pantalla abre cada entrada del cajón ({@code null} si no es ninguna). */
    private Class<?> destinoDelCajon(int id) {
        if (id == R.id.nav_coleccion) return BibliotecaActivity.class;
        if (id == R.id.nav_buscar) return BuscarActivity.class;
        if (id == R.id.nav_anadir) return AltaCancionActivity.class;
        if (id == R.id.nav_playlists) return LoginActivity.class;
        if (id == R.id.nav_reproducciones) return ReproduccionActivity.class;
        if (id == R.id.nav_noche) return OyenteActivity.class;
        if (id == R.id.nav_invitar) return InvitarCorreoActivity.class;
        if (id == R.id.nav_acerca_de) return AcercaDeActivity.class;
        return null;
    }

    private void abrir(Class<?> pantalla) {
        startActivity(new Intent(this, pantalla));
    }

    /**
     * Añade a la vista un padding del tamaño de las barras del sistema y del notch.
     * Así la toolbar baja hasta quedar por debajo de la cámara, en vez de chocar con ella.
     */
    private void aplicarInsets(int idVista) {
        View vista = findViewById(idVista);
        ViewCompat.setOnApplyWindowInsetsListener(vista, (v, insets) -> {
            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
    }
}
