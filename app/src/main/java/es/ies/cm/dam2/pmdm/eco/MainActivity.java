package es.ies.cm.dam2.pmdm.eco;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    int counterValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView counter = findViewById(R.id.counter);
        counter.setText(String.valueOf(counterValue));

        Button checkButton = findViewById(R.id.checkButton);
        checkButton.setOnClickListener(v -> {
            counterValue += 1;
            counter.setText(String.valueOf(counterValue));
            if(counterValue == 10){
                Intent intent = new Intent(this, AltaCancionActivity.class);
                startActivity(intent);
            }
        });

        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> {
            counterValue = 0;
            counter.setText(String.valueOf(counterValue));
        });

        TextView texto = findViewById(R.id.Created);
        texto.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaletaActivity.class);
            startActivity(intent);
        });

        TextView appName = findViewById(R.id.appName);
        appName.setOnClickListener(v -> {
            Intent intent = new Intent(this, PreubaActivity.class);
            startActivity(intent);
        });
    }
}