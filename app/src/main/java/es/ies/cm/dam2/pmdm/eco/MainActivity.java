package es.ies.cm.dam2.pmdm.eco;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(getString(R.string.ECO_MAIN_ACTIVITY), "Estoy en onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(getString(R.string.ECO_MAIN_ACTIVITY), "Estoy en onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(getString(R.string.ECO_MAIN_ACTIVITY), "Estoy en onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("@string/ECO_MAIN_ACTIVITY", "Estoy en onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("@string/ECO_MAIN_ACTIVITY", "Estoy en onDestroy");
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("@string/ECO_MAIN_ACTIVITY", "Estoy en onRestart");
    }
}