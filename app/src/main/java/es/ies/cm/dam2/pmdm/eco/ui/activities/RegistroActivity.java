package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.data.room.AppDatabase;
import es.ies.cm.dam2.pmdm.eco.data.sqlite.EcoDB;

/**
 * Proyecto: registro de usuarios (la C del CRUD de {@code usuarios}).
 * <p>
 * Validaciones: campos obligatorios, nombre de 3 caracteres o más, clave de 4
 * o más, las dos claves iguales y nombre libre (la columna es UNIQUE). Si todo
 * va bien, devuelve el {@code _id} nuevo al login, que entra directamente.
 */
public class RegistroActivity extends AppCompatActivity {

    private static final int MIN_NOMBRE = 3;
    private static final int MIN_CLAVE = 4;

    private EcoDB ecoDB;
    private TextInputLayout usuarioLayout, claveLayout, repetirLayout;
    private TextInputEditText usuarioEdit, claveEdit, repetirEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ecoDB = EcoDB.getInstance(this);
        usuarioLayout = findViewById(R.id.usuarioRegistroLayout);
        claveLayout = findViewById(R.id.claveRegistroLayout);
        repetirLayout = findViewById(R.id.repetirRegistroLayout);
        usuarioEdit = findViewById(R.id.usuarioRegistroEdit);
        claveEdit = findViewById(R.id.claveRegistroEdit);
        repetirEdit = findViewById(R.id.repetirRegistroEdit);

        findViewById(R.id.registrarButton).setOnClickListener(v -> registrar());
    }

    private void registrar() {
        usuarioLayout.setError(null);
        claveLayout.setError(null);
        repetirLayout.setError(null);

        String nombre = texto(usuarioEdit);
        String clave = texto(claveEdit);
        String repetir = texto(repetirEdit);

        boolean ok = true;
        if (nombre.length() < MIN_NOMBRE) {
            usuarioLayout.setError(getString(R.string.registro_error_nombre, MIN_NOMBRE));
            ok = false;
        }
        if (clave.length() < MIN_CLAVE) {
            claveLayout.setError(getString(R.string.registro_error_clave, MIN_CLAVE));
            ok = false;
        } else if (!clave.equals(repetir)) {
            repetirLayout.setError(getString(R.string.registro_error_distintas));
            ok = false;
        }
        if (!ok) {
            return;
        }

        AppDatabase.executor.execute(() -> {
            // Se comprueba antes para dar un mensaje claro; el UNIQUE de la
            // columna es la garantía final (insert devuelve -1 si choca).
            long id = ecoDB.existeUsuario(nombre) ? -1 : ecoDB.registrar(nombre, clave);
            runOnUiThread(() -> {
                if (id == -1) {
                    usuarioLayout.setError(getString(R.string.registro_error_existe));
                    return;
                }
                Intent respuesta = new Intent();
                respuesta.putExtra(PlaylistsActivity.EXTRA_USUARIO_ID, (int) id);
                setResult(RESULT_OK, respuesta);
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
