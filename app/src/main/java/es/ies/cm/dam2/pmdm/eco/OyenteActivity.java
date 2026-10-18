package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * P5.1 (pantalla 1): se escribe el nombre del oyente y se pulsa Continuar.
 * <p>
 * La confirmación de asistencia la responde {@link ConfirmarAsistenciaActivity}
 * y vuelve aquí como resultado: por eso se lanza con un
 * {@link ActivityResultLauncher} y no con {@code startActivity(...)}.
 */
public class OyenteActivity extends AppCompatActivity {

    /**
     * El lanzador se registra como ATRIBUTO de la clase (una sola vez, antes de
     * que la activity esté iniciada). La respuesta llega en el callback.
     */
    private final ActivityResultLauncher<Intent> lanzadorAsistencia =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), respuesta -> {
                // Caso "flecha Atrás": el código NO es RESULT_OK -> no hay nada que leer.
                if (respuesta.getResultCode() == RESULT_OK && respuesta.getData() != null) {
                    boolean acepta = respuesta.getData()
                            .getBooleanExtra(ConfirmarAsistenciaActivity.EXTRA_ASISTENCIA, false);
                    mostrarMensaje(acepta);
                }
            });

    /** Nombre escrito al pulsar Continuar: no viaja en el Intent, se guarda aquí. */
    private String nombre = "";

    private EditText nombreEdit;
    private TextView resultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_oyente);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nombreEdit = findViewById(R.id.oyenteNombreEdit);
        resultado = findViewById(R.id.resultadoAsistencia);
        Button continuar = findViewById(R.id.continuarButton);

        continuar.setOnClickListener(v -> {
            String texto = nombreEdit.getText().toString().trim();
            if (texto.isEmpty()) {
                Toast.makeText(this, R.string.falta_nombre, Toast.LENGTH_SHORT).show();
                return;
            }
            nombre = texto;
            // Se abre con EL LANZADOR, para que esta pantalla reciba la respuesta.
            lanzadorAsistencia.launch(new Intent(this, ConfirmarAsistenciaActivity.class));
        });
    }

    /** Pinta el mensaje final juntando el nombre (de aquí) y el resultado (de la otra pantalla). */
    private void mostrarMensaje(boolean acepta) {
        String decision = getString(acepta ? R.string.aceptado : R.string.cancelado);
        resultado.setText(getString(R.string.mensaje_asistencia, nombre, decision));
    }
}
