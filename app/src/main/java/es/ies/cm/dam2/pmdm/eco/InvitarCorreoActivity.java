package es.ies.cm.dam2.pmdm.eco;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * P5.2 Invitación por correo: recoge destinatario, asunto y texto y se lo pasa a
 * otra aplicación con un <b>Intent implícito</b> ({@code ACTION_SEND}).
 * <p>
 * Esta app no envía nada ni necesita permisos: solo pide a otra app que lo haga.
 */
public class InvitarCorreoActivity extends AppCompatActivity {

    private EditText destinatarioEdit;
    private EditText asuntoEdit;
    private EditText textoEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invitar_correo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        destinatarioEdit = findViewById(R.id.destinatarioEdit);
        asuntoEdit = findViewById(R.id.asuntoEdit);
        textoEdit = findViewById(R.id.textoEdit);

        Button enviar = findViewById(R.id.enviarButton);
        enviar.setOnClickListener(v -> enviarCorreo());
    }

    private void enviarCorreo() {
        // El destinatario va como ARRAY de textos, no como un texto suelto.
        String[] destinatarios = {destinatarioEdit.getText().toString().trim()};

        Intent correo = new Intent(Intent.ACTION_SEND);
        correo.putExtra(Intent.EXTRA_EMAIL, destinatarios);
        correo.putExtra(Intent.EXTRA_SUBJECT, asuntoEdit.getText().toString());
        correo.putExtra(Intent.EXTRA_TEXT, textoEdit.getText().toString());
        correo.setType("message/rfc822");

        try {
            // createChooser hace que salga el diálogo "Enviar con...".
            startActivity(Intent.createChooser(correo, getString(R.string.enviar)));
        } catch (ActivityNotFoundException e) {
            // No hay ninguna app que sepa enviar correos.
            Toast.makeText(this, R.string.sin_apps_correo, Toast.LENGTH_SHORT).show();
        }
    }
}
