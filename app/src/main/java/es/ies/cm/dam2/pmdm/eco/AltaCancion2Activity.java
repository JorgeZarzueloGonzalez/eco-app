package es.ies.cm.dam2.pmdm.eco;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * P3.1 Teclado de puntuación: 9 botones (1-9) en tabla 3x3, todos con el mismo
 * estilo compartido ({@code @style/TeclaPuntuacion}). El número pulsado se
 * muestra en el TextView de resultado.
 */
public class AltaCancion2Activity extends AppCompatActivity {

    /** Ids de las 9 teclas, en orden del 1 al 9. */
    private static final int[] TECLAS = {
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alta_cancion2);

        // Al aplicar los insets hay que SUMAR el padding que ya trae el layout:
        // si se llama a setPadding con los valores de las barras, se pierde el padding propio.
        final View raiz = findViewById(R.id.main);
        final int padIzquierda = raiz.getPaddingLeft();
        final int padArriba = raiz.getPaddingTop();
        final int padDerecha = raiz.getPaddingRight();
        final int padAbajo = raiz.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(padIzquierda + systemBars.left,
                    padArriba + systemBars.top,
                    padDerecha + systemBars.right,
                    padAbajo + systemBars.bottom);
            return insets;
        });

        TextView scoreResult = findViewById(R.id.scoreResult);

        // Los 9 botones reaccionan igual: solo cambia el número que se pinta.
        for (int i = 0; i < TECLAS.length; i++) {
            final int numero = i + 1;
            Button tecla = findViewById(TECLAS[i]);
            tecla.setOnClickListener(v -> scoreResult.setText(getString(R.string.score, numero)));
        }
    }
}
