package es.ies.cm.dam2.pmdm.eco;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AltaCancionActivity extends AppCompatActivity {

    private int artistNumberValue = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alta_cancion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView artistNumber = findViewById(R.id.artistNumber);
        artistNumber.setText(getString(R.string.numbers_of_artist, artistNumberValue));

        TextView songName = findViewById(R.id.songName);
        EditText songNameEdit = findViewById(R.id.songNameEdit);

        TextView puss = findViewById(R.id.puss);

        Button addButton = findViewById(R.id.addButton);
        Button resButton = findViewById(R.id.resButton);
        Button nameButton = findViewById(R.id.nameButton);
        CheckBox checkBoxBiblioteca = findViewById(R.id.checkBoxBiblioteca);
        Button boxButton = findViewById(R.id.boxButton);

        addButton.setOnClickListener(v -> {
            artistNumberValue += 1;
            if (artistNumberValue == 4){
                Toast.makeText( AltaCancionActivity.this, "Maximo numero de artistas", Toast.LENGTH_SHORT).show();
                addButton.setEnabled(false);
            }else {
                if(!resButton.isEnabled()) {
                    resButton.setEnabled(true);
                }
            }
            artistNumber.setText(getString(R.string.numbers_of_artist, artistNumberValue));
        });

        resButton.setOnClickListener(v -> {
            artistNumberValue -= 1;
            if (artistNumberValue == 1){
                Toast.makeText( AltaCancionActivity.this, "Minimo numero de artistas", Toast.LENGTH_SHORT).show();
                resButton.setEnabled(false);
            }else {
                if(!addButton.isEnabled()){
                    addButton.setEnabled(true);
                }
            }
            artistNumber.setText(getString(R.string.numbers_of_artist, artistNumberValue));
        });

        nameButton.setOnClickListener(v -> {
            songName.setText(songNameEdit.getText());
            songNameEdit.setText("");
        });


        checkBoxBiblioteca.setOnCheckedChangeListener((v, isChecked) -> {
            if(checkBoxBiblioteca.isChecked()){
                boxButton.setEnabled(false);
            }else{
                boxButton.setEnabled(true);
            }
        });

        boxButton.setOnClickListener(v -> {
            puss.setText(getString(R.string.button_pussed));
        });

    }
}