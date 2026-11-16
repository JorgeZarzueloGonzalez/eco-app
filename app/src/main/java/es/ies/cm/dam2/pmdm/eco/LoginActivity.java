package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * P12 Login: comprueba usuario y clave en la tabla {@code usuarios} de
 * {@code eco.db}. Si existen, abre {@link FavoritosActivity} pasando el
 * {@code _id} del usuario; si no, avisa con un Toast.
 */
public class LoginActivity extends AppCompatActivity {

    /** Clave del extra con el {@code _id} del usuario que inicia sesión. */
    public static final String EXTRA_USUARIO_ID = "extra_usuario_id";

    private UsuarioRepository usuarioRepository;
    private EditText usuarioEdit;
    private EditText claveEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setTitle(R.string.login_titulo);

        usuarioRepository = new UsuarioRepository(this);
        usuarioEdit = findViewById(R.id.usuarioLoginEdit);
        claveEdit = findViewById(R.id.claveLoginEdit);

        findViewById(R.id.entrarButton).setOnClickListener(v -> iniciarSesion());
    }

    /** Valida las credenciales y, si son correctas, entra en los favoritos. */
    private void iniciarSesion() {
        String nombre = usuarioEdit.getText().toString().trim();
        String clave = claveEdit.getText().toString();

        if (nombre.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, R.string.login_falta_datos, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!usuarioRepository.validarCredenciales(nombre, clave)) {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario usuario = usuarioRepository.getUsuarioPorNombre(nombre);
        if (usuario == null) {
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, FavoritosActivity.class);
        intent.putExtra(FavoritosActivity.EXTRA_USUARIO_ID, usuario.getId());
        startActivity(intent);
        finish();
    }
}
