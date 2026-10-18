package es.ies.cm.dam2.pmdm.eco;

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    int counterValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView counter = findViewById(R.id.counter);
        counter.setText(getString(R.string.reproductions, counterValue));

        Button checkButton = findViewById(R.id.checkButton);
        checkButton.setOnClickListener(v -> {
            counterValue += 1;
            counter.setText(getString(R.string.reproductions, counterValue));
            if(counterValue == 10){
                Intent intent = new Intent(this, AltaCancionActivity.class);
                startActivity(intent);
            }
        });

        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> {
            counterValue = 0;
            counter.setText(getString(R.string.reproductions, counterValue));
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

        TextView appName = findViewById(R.id.appName);
        appName.setOnClickListener(v -> {
            Intent intent = new Intent(this, PreubaActivity.class);
            startActivity(intent);
        });
    }
}