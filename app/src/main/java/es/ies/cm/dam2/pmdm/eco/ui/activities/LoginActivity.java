package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import es.ies.cm.dam2.pmdm.eco.model.Usuario;

/**
 * Login: comprueba usuario y clave en la tabla {@code usuarios} de
 * {@code eco.db}.
 * <ul>
 *     <li>Correctos: abre {@link PlaylistsActivity} con el {@code _id} del
 *     usuario (el id viaja entre pantallas) y se cierra.</li>
 *     <li>Incorrectos: aviso en el campo de la clave.</li>
 *     <li>"Crear cuenta": abre {@link RegistroActivity}; si el registro
 *     termina bien, devuelve el id nuevo y se entra directamente.</li>
 * </ul>
 */
public class LoginActivity extends AppCompatActivity {

    private EcoDB ecoDB;
    private TextInputLayout usuarioLayout, claveLayout;
    private TextInputEditText usuarioEdit, claveEdit;

    /** Abre el registro y recoge su respuesta (el id del usuario creado). */
    private final ActivityResultLauncher<Intent> registro = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == RESULT_OK && resultado.getData() != null) {
                    entrar(resultado.getData().getIntExtra(PlaylistsActivity.EXTRA_USUARIO_ID, 0));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
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
        usuarioLayout = findViewById(R.id.usuarioLoginLayout);
        claveLayout = findViewById(R.id.claveLoginLayout);
        usuarioEdit = findViewById(R.id.usuarioLoginEdit);
        claveEdit = findViewById(R.id.claveLoginEdit);

        findViewById(R.id.entrarButton).setOnClickListener(v -> iniciarSesion());
        findViewById(R.id.crearCuentaButton).setOnClickListener(v ->
                registro.launch(new Intent(this, RegistroActivity.class)));
    }

    /** Valida las credenciales (en el executor) y, si son correctas, entra. */
    private void iniciarSesion() {
        usuarioLayout.setError(null);
        claveLayout.setError(null);
        String nombre = texto(usuarioEdit);
        String clave = claveEdit.getText() == null ? "" : claveEdit.getText().toString();

        if (nombre.isEmpty()) {
            usuarioLayout.setError(getString(R.string.error_obligatorio));
            return;
        }
        if (clave.isEmpty()) {
            claveLayout.setError(getString(R.string.error_obligatorio));
            return;
        }

        AppDatabase.executor.execute(() -> {
            Usuario usuario = ecoDB.login(nombre, clave);
            runOnUiThread(() -> {
                if (usuario == null) {
                    claveLayout.setError(getString(R.string.login_error));
                } else {
                    entrar(usuario.getId());
                }
            });
        });
    }

    private void entrar(int usuarioId) {
        if (usuarioId <= 0) {
            return;
        }
        Intent intent = new Intent(this, PlaylistsActivity.class);
        intent.putExtra(PlaylistsActivity.EXTRA_USUARIO_ID, usuarioId);
        startActivity(intent);
        finish(); // Atrás desde las playlists vuelve al inicio, no al login
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
