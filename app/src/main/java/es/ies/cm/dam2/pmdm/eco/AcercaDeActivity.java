package es.ies.cm.dam2.pmdm.eco;

import android.content.ClipData;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * P6.3 "Acerca de Eco": un único TextView con el autor.
 * <p>
 * Ese mismo texto hace de <b>origen</b> (se arrastra con el dedo) y de
 * <b>destino</b> (recibe la caída). Al soltar Encima, la pantalla se cierra.
 */
public class AcercaDeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acerca_de);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.autor), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView autor = findViewById(R.id.autor);

        // ORIGEN: al tocar el texto, se empieza el arrastre con un ClipData
        // (los datos que viajan con el dedo) y la sombra por defecto.
        autor.setOnTouchListener((vista, evento) -> {
            if (evento.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData dato = ClipData.newPlainText("autor", autor.getText());
                vista.startDragAndDrop(dato, new View.DragShadowBuilder(vista), null, 0);
                return true;
            }
            return false;
        });

        // DESTINO: el mismo TextView recibe la caída.
        autor.setOnDragListener((vista, evento) -> {
            int accion = evento.getAction();
            // ACTION_DRAG_STARTED y ACTION_DROP tienen que devolver true.
            if (accion == DragEvent.ACTION_DRAG_STARTED || accion == DragEvent.ACTION_DROP) {
                if (accion == DragEvent.ACTION_DROP) {
                    finish();
                }
                return true;
            }
            return false;
        });
    }
}
