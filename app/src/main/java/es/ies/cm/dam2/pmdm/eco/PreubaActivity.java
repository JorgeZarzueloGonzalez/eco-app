package es.ies.cm.dam2.pmdm.eco;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class PreubaActivity extends AppCompatActivity {

    Spinner asignaturas;
    ListView modulos;
    String[] misAsignaturas = new String[]{"Lengua", "Matematicas", "TIC", "Lengua", "Matematicas", "TIC", "Lengua", "Matematicas", "TIC", "Lengua", "Matematicas", "TIC", "Lengua", "Matematicas", "TIC"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_preuba);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        asignaturas = findViewById(R.id.spinner);
        modulos = findViewById(R.id.listView);
        ArrayAdapter<String> adaptadorSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, misAsignaturas);
        //ArrayAdapter<String> adaptadorListView = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, misAsignaturas);

        asignaturas.setAdapter(adaptadorSpinner);
        //modulos.setAdapter(adaptadorListView);

        List<Asignatura> asignaturas2 = new ArrayList<>();
        asignaturas2.add(new Asignatura("Matematicas", "1ESO", "Clautido Moyano"));
        asignaturas2.add(new Asignatura("Matematicas", "2ESO", "Clautido Moyano"));
        asignaturas2.add(new Asignatura("Matematicas", "3ESO", "Clautido Moyano"));
        asignaturas2.add(new Asignatura("Matematicas", "4ESO", "Clautido Moyano"));
        asignaturas2.add(new Asignatura("Lengua", "1ESO", "Clautido Moyano"));
        asignaturas2.add(new Asignatura("Lengua", "2ESO", "Clautido Moyano"));
        asignaturas2.add(new Asignatura("Lengua", "3ESO", "Clautido Moyano"));
        asignaturas2.add(new Asignatura("Lengua", "4ESO", "Clautido Moyano"));

        AdaptadorAsignatura miAdaptador = new AdaptadorAsignatura(this, asignaturas2);
        modulos.setAdapter(miAdaptador);

        asignaturas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(parent.getContext(), "ACTIVITY 2: " + parent.getAdapter().getItem(position), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}