package es.ies.cm.dam2.pmdm.eco.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import java.util.Calendar;

/**
 * Proyecto: selector de FECHA (DatePicker) dentro de un DialogFragment.
 * <p>
 * Recibe la fecha de partida en milisegundos y devuelve año, mes y día con la
 * Fragment Result API (clave {@link #CLAVE}). Al girar el móvil el diálogo
 * se recrea solo, con la fecha que tuviera marcada.
 */
public class FechaDialogFragment extends DialogFragment {

    public static final String CLAVE = "fecha_elegida";
    public static final String ANIO = "anio";
    public static final String MES = "mes";
    public static final String DIA = "dia";

    private static final String ARG_MILIS = "milis";

    public static void mostrar(FragmentManager fm, long milisIniciales) {
        Bundle args = new Bundle();
        args.putLong(ARG_MILIS, milisIniciales);
        FechaDialogFragment dialogo = new FechaDialogFragment();
        dialogo.setArguments(args);
        dialogo.show(fm, CLAVE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(requireArguments().getLong(ARG_MILIS));

        DatePickerDialog dialogo = new DatePickerDialog(requireContext(),
                (picker, anio, mes, dia) -> {
                    Bundle respuesta = new Bundle();
                    respuesta.putInt(ANIO, anio);
                    respuesta.putInt(MES, mes);   // OJO: enero = 0
                    respuesta.putInt(DIA, dia);
                    getParentFragmentManager().setFragmentResult(CLAVE, respuesta);
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        // Una escucha no puede ser del futuro
        dialogo.getDatePicker().setMaxDate(System.currentTimeMillis());
        return dialogo;
    }
}
