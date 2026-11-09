package es.ies.cm.dam2.pmdm.eco;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    int counterValue = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Con EdgeToEdge la ventana también ocupa la barra de estado y el notch,
        // así que hay que apartar a mano el contenido de esa zona.
        // (El cajón lo hace él solo con android:fitsSystemWindows="true" en el XML.)
        aplicarInsets(R.id.contenido);   // incluye la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggleButton = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggleButton);
        toggleButton.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                drawerLayout.close();
                return true;
            }
            if (id == R.id.nav_coleccion) {
                startActivity(new Intent(this, BibliotecaActivity.class));
                drawerLayout.close();
                return true;
            }
            if (id == R.id.nav_buscar) {
                startActivity(new Intent(this, BuscarActivity.class));
                drawerLayout.close();
                return true;
            }
            if (id == R.id.nav_anadir) {
                startActivity(new Intent(this, AltaCancionActivity.class));
                drawerLayout.close();
                return true;
            }
            if (id == R.id.nav_ajustes) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Ajustes")
                        .setMessage("Ajustes Disponible en la práctica 20")
                        .setNeutralButton("Ok", null)
                        .show();
                drawerLayout.close();
                return true;
            }
            if (id == R.id.nav_acerca_de) {
                startActivity(new Intent(this, AcercaDeActivity.class));
                drawerLayout.close();
                return true;
            }
            return false;
        });

        Button altaButton = findViewById(R.id.altaButton);
        altaButton.setOnClickListener(v ->
                startActivity(new Intent(this, AltaCancionActivity.class)));

        Button oyenteButton = findViewById(R.id.oyenteButton);
        oyenteButton.setOnClickListener(v ->
                startActivity(new Intent(this, OyenteActivity.class)));

        Button invitarButton = findViewById(R.id.invitarButton);
        invitarButton.setOnClickListener(v ->
                startActivity(new Intent(this, InvitarCorreoActivity.class)));

        // ===================== P6.1 Abrir la web de Eco =====================
        Button webButton = findViewById(R.id.webButton);
        webButton.setOnClickListener(v -> {
            Intent verWeb = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.web_eco)));
            try {
                startActivity(verWeb);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.sin_navegador, Toast.LENGTH_SHORT).show();
            }
        });

        // ===================== P6.2 Compartir: segunda actividad =====================
        Button compartirButton = findViewById(R.id.compartirButton);
        compartirButton.setOnClickListener(v ->
                startActivity(new Intent(this, CompartirActivity.class)));

        // ===================== P7 Buscar canción =====================
        Button buscarButton = findViewById(R.id.buscarButton);
        buscarButton.setOnClickListener(v ->
                startActivity(new Intent(this, BuscarActivity.class)));

        // ===================== P8 La biblioteca =====================
        Button bibliotecaButton = findViewById(R.id.bibliotecaButton);
        bibliotecaButton.setOnClickListener(v ->
                startActivity(new Intent(this, BibliotecaActivity.class)));

        // ===================== P6.3 Drag &amp; Drop hasta la zona de destino =====================
        TextView dragSource = findViewById(R.id.dragSource);
        TextView dropTarget = findViewById(R.id.dropTarget);

        // ORIGEN: al tocar, empieza el arrastre (hace falta un ClipData).
        dragSource.setOnTouchListener((vista, evento) -> {
            if (evento.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData dato = ClipData.newPlainText("eco", getString(R.string.app_name));
                vista.startDragAndDrop(dato, new View.DragShadowBuilder(vista), null, 0);
                return true;
            }
            return false;
        });

        // DESTINO: si se suelta aquí encima, se abre "Acerca de Eco".
        dropTarget.setOnDragListener((vista, evento) -> {
            int accion = evento.getAction();
            if (accion == DragEvent.ACTION_DRAG_STARTED) {
                // El destino acepta el arrastre: si devolviera false, el drop no llegaría.
                return true;
            }
            if (accion == DragEvent.ACTION_DRAG_ENTERED || accion == DragEvent.ACTION_DRAG_EXITED) {
                vista.setBackgroundColor(ContextCompat.getColor(this,
                        accion == DragEvent.ACTION_DRAG_ENTERED
                                ? R.color.accent_fill : R.color.surface_container));
                return true;
            }
            if (accion == DragEvent.ACTION_DROP) {
                startActivity(new Intent(this, AcercaDeActivity.class));
                return true;
            }
            if (accion == DragEvent.ACTION_DRAG_ENDED) {
                vista.setBackgroundColor(ContextCompat.getColor(this, R.color.surface_container));
                return true;
            }
            return false;
        });

        TextView texto = findViewById(R.id.Created);
        texto.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaletaActivity.class);
            startActivity(intent);
        });
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