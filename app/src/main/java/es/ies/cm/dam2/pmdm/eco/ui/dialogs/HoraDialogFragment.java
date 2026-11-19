package es.ies.cm.dam2.pmdm.eco.ui.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import java.util.Calendar;

/**
 * Proyecto: selector de HORA (TimePicker) dentro de un DialogFragment.
 * <p>
 * Se abre justo después de {@link FechaDialogFragment}. Devuelve hora y minuto
 * con la Fragment Result API (clave {@link #CLAVE}). Usa el formato de 12 o
 * 24 horas que tenga configurado el móvil.
 */
public class HoraDialogFragment extends DialogFragment {

    public static final String CLAVE = "hora_elegida";
    public static final String HORA = "hora";
    public static final String MINUTO = "minuto";

    private static final String ARG_MILIS = "milis";

    public static void mostrar(FragmentManager fm, long milisIniciales) {
        Bundle args = new Bundle();
        args.putLong(ARG_MILIS, milisIniciales);
        HoraDialogFragment dialogo = new HoraDialogFragment();
        dialogo.setArguments(args);
        dialogo.show(fm, CLAVE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(requireArguments().getLong(ARG_MILIS));

        return new TimePickerDialog(requireContext(),
                (picker, hora, minuto) -> {
                    Bundle respuesta = new Bundle();
                    respuesta.putInt(HORA, hora);
                    respuesta.putInt(MINUTO, minuto);
                    getParentFragmentManager().setFragmentResult(CLAVE, respuesta);
                },
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(requireContext()));
    }
}
