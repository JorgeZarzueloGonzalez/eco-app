package es.ies.cm.dam2.pmdm.eco.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.ies.cm.dam2.pmdm.eco.R;
import es.ies.cm.dam2.pmdm.eco.data.repository.CancionRepository;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import es.ies.cm.dam2.pmdm.eco.ui.adapters.AdaptadorCancionRecycler;
import java.util.ArrayList;

/**
 * P12 Buscador de la biblioteca: filtra por nombre (empieza por o contiene) y
 * por valoración mínima, consultando la tabla {@code canciones} de {@code eco.db}.
 * <p>
 * Sustituye a los filtros estáticos de la P7. Los resultados se pintan con el
 * mismo {@link AdaptadorCancionRecycler} de la biblioteca y, al tocar uno, se
 * abre su ficha.
 */
public class BuscarActivity extends AppCompatActivity
        implements AdaptadorCancionRecycler.OyenteCancion {

    /** Los resultados de la última búsqueda (la lista que pinta el adaptador). */
    private final ArrayList<Cancion> resultados = new ArrayList<>();

    private CancionRepository cancionRepository;
    private AdaptadorCancionRecycler adaptador;
    private EditText nombreEdit;
    private RadioButton empiezaRadio;
    private RatingBar valoracionRating;
    private TextView resultadosVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buscar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cancionRepository = new CancionRepository(this);

        nombreEdit = findViewById(R.id.nombreBuscarEdit);
        empiezaRadio = findViewById(R.id.radioEmpieza);
        valoracionRating = findViewById(R.id.valoracionBuscarRating);
        resultadosVacio = findViewById(R.id.resultadosVacio);

        RecyclerView resultadosRecycler = findViewById(R.id.resultadosRecycler);
        resultadosRecycler.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorCancionRecycler(resultados, this);
        resultadosRecycler.setAdapter(adaptador);

        findViewById(R.id.buscarButton).setOnClickListener(v -> buscar());

        // La lupa del teclado también lanza la búsqueda.
        nombreEdit.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                buscar();
                return true;
            }
            return false;
        });
    }

    /** Al volver a la pantalla se repite la búsqueda por si cambió la base de datos. */
    @Override
    protected void onResume() {
        super.onResume();
        if (adaptador != null) {
            buscar();
        }
    }

    /** Consulta la base de datos con los filtros actuales y repinta la lista. */
    private void buscar() {
        String texto = nombreEdit.getText().toString().trim();
        boolean empiezaPor = empiezaRadio.isChecked();
        int valoracionMinima = Math.round(valoracionRating.getRating());

        resultados.clear();
        resultados.addAll(cancionRepository.buscar(texto, empiezaPor, valoracionMinima));
        adaptador.notifyDataSetChanged();

        resultadosVacio.setVisibility(resultados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void alHacerClicCorto(int posicion) {
        if (posicion == RecyclerView.NO_POSITION) return;

        Intent intent = new Intent(this, FichaCancionActivity.class);
        intent.putExtra(BibliotecaActivity.EXTRA_CANCION, resultados.get(posicion));
        startActivity(intent);
    }

    @Override
    public void alHacerClicLargo(int posicion, View itemview) {
        // El buscador no edita ni borra: eso se hace desde la biblioteca.
    }
}
