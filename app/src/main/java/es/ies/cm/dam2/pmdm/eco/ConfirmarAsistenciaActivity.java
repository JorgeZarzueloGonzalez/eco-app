package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * P5.1 (pantalla 2): se acepta o se cancela la asistencia a la Noche de Eco.
 * <p>
 * Cada botón devuelve el resultado a la pantalla anterior: crea el Intent de
 * respuesta, mete el extra, llama a {@code setResult(RESULT_OK, ...)} y
 * DESPUÉS cierra la pantalla con {@code finish()}.
 */
public class ConfirmarAsistenciaActivity extends AppCompatActivity {

    /** Clave del extra que viaja de vuelta. Es la misma constante que lee la pantalla 1. */
    public static final String EXTRA_ASISTENCIA = "extra_asistencia";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirmar_asistencia);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button aceptar = findViewById(R.id.aceptarButton);
        Button cancelar = findViewById(R.id.cancelarButton);

        aceptar.setOnClickListener(v -> responder(true));
        cancelar.setOnClickListener(v -> responder(false));
    }

    /** Devuelve la respuesta a la pantalla anterior y se cierra. */
    private void responder(boolean acepta) {
        Intent respuesta = new Intent();
        respuesta.putExtra(EXTRA_ASISTENCIA, acepta);
        setResult(RESULT_OK, respuesta);
        finish();
    }
}
