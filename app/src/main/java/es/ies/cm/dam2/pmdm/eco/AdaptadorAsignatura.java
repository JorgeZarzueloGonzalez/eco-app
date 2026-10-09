package es.ies.cm.dam2.pmdm.eco;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorAsignatura extends ArrayAdapter<Asignatura> {
    ArrayList<Asignatura> asignaturas;
    private Context contexto;

    public AdaptadorAsignatura(@NonNull Context context, @NonNull List<Asignatura> asignaturas) {
        super(context, R.layout.milayout, asignaturas);
        this.asignaturas = new ArrayList<>(asignaturas);
        this.contexto = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(contexto);
        View vista = inflater.inflate(R.layout.milayout, null);
        TextView viewNombreAsignatura = vista.findViewById(R.id.nombreAsignatura);
        TextView viewCursoAsignatura = vista.findViewById(R.id.cursoAsignatura);
        TextView viewInstituto = vista.findViewById(R.id.insituto);
        Asignatura asignatura = this.asignaturas.get(position);
        viewNombreAsignatura.setText(asignatura.getNombre());
        viewCursoAsignatura.setText(asignatura.getCurso());
        viewInstituto.setText(asignatura.getInstituto());

        return vista;
    }
}
