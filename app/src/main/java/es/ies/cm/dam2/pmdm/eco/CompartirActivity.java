package es.ies.cm.dam2.pmdm.eco;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * P6.2 Compartir una canción por WhatsApp.
 * <p>
 * El botón Enviar empieza apagado y se enciende solo cuando el campo tiene texto
 * (mientras el usuario escribe). Al enviar se abre WhatsApp con el mensaje
 * preparado; si no está instalado, se abre su ficha en Google Play.
 */
public class CompartirActivity extends AppCompatActivity {

    /** Paquete (el "DNI") de WhatsApp. */
    private static final String PAQUETE_WHATSAPP = "com.whatsapp";

    /** URL de Google Play que abre la ficha de WhatsApp. */
    private static final String URL_WHATSAPP = "market://details?id=" + PAQUETE_WHATSAPP;

    private EditText cancionEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compartir);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cancionEdit = findViewById(R.id.cancionEdit);
        Button enviar = findViewById(R.id.enviarWhatsAppButton);

        // Mientras escribe: el botón sigue al contenido del campo.
        cancionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se usa
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se usa
            }

            @Override
            public void afterTextChanged(Editable s) {
                enviar.setEnabled(!s.toString().trim().isEmpty());
            }
        });

        enviar.setOnClickListener(v -> compartirPorWhatsApp());
    }

    private void compartirPorWhatsApp() {
        String texto = cancionEdit.getText().toString().trim();

        Intent compartir = new Intent(Intent.ACTION_SEND);
        compartir.setType("text/plain");
        compartir.putExtra(Intent.EXTRA_TEXT, texto);
        // Con setPackage solo puede responder WhatsApp.
        compartir.setPackage(PAQUETE_WHATSAPP);

        try {
            startActivity(compartir);
        } catch (ActivityNotFoundException e) {
            // No hay WhatsApp: a la ficha de Google Play para instalarlo.
            Intent tienda = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_WHATSAPP));
            try {
                startActivity(tienda);
            } catch (ActivityNotFoundException sinTienda) {
                Toast.makeText(this, R.string.sin_whatsapp, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
