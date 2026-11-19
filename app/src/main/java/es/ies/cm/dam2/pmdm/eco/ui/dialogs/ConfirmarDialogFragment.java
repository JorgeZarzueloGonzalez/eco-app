package es.ies.cm.dam2.pmdm.eco.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import es.ies.cm.dam2.pmdm.eco.R;

/**
 * Proyecto: diálogo propio para confirmar una acción (borrar una canción, una
 * playlist, una reproducción…).
 * <p>
 * ¿Por qué un {@link DialogFragment} y no un AlertDialog suelto? Porque Android
 * lo vuelve a crear solo al girar el móvil: el diálogo no desaparece y el
 * botón sigue funcionando. Por eso no recibe un listener (se perdería al girar):
 * avisa con la <b>Fragment Result API</b>. La activity escucha la clave
 * ({@code requestKey}) en {@code onCreate} y recibe el {@code id} del elemento.
 */
public class ConfirmarDialogFragment extends DialogFragment {

    /** Clave del Bundle de respuesta con el id del elemento confirmado. */
    public static final String RESULTADO_ID = "id";

    private static final String ARG_CLAVE = "clave";
    private static final String ARG_TITULO = "titulo";
    private static final String ARG_MENSAJE = "mensaje";
    private static final String ARG_ID = "id";

    /**
     * Enseña el diálogo.
     *
     * @param clave   la clave con la que se devolverá la respuesta
     * @param titulo  título del diálogo
     * @param mensaje pregunta que se hace
     * @param id      identificador del elemento afectado (vuelve en la respuesta)
     */
    public static void mostrar(FragmentManager fm, String clave, String titulo,
                               String mensaje, int id) {
        // Los datos van en los argumentos: Android los guarda y los restaura al girar.
        Bundle args = new Bundle();
        args.putString(ARG_CLAVE, clave);
        args.putString(ARG_TITULO, titulo);
        args.putString(ARG_MENSAJE, mensaje);
        args.putInt(ARG_ID, id);

        ConfirmarDialogFragment dialogo = new ConfirmarDialogFragment();
        dialogo.setArguments(args);
        dialogo.show(fm, clave);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(args.getString(ARG_TITULO))
                .setMessage(args.getString(ARG_MENSAJE))
                .setPositiveButton(R.string.confirmar_si, (dialog, which) -> {
                    Bundle respuesta = new Bundle();
                    respuesta.putInt(RESULTADO_ID, args.getInt(ARG_ID));
                    getParentFragmentManager().setFragmentResult(args.getString(ARG_CLAVE), respuesta);
                })
                .setNegativeButton(R.string.cancelar, null)
                .create();
    }
}
