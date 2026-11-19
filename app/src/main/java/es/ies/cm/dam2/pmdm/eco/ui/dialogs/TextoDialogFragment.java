package es.ies.cm.dam2.pmdm.eco.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import es.ies.cm.dam2.pmdm.eco.R;

/**
 * Proyecto: diálogo con uno o dos campos de texto. Se usa para crear y
 * renombrar playlists (un campo) y para cambiar la contraseña (dos campos:
 * la actual y la nueva).
 * <p>
 * Como {@link ConfirmarDialogFragment}, devuelve la respuesta con la Fragment
 * Result API y sobrevive al giro (lo escrito en los campos también, porque
 * tienen id).
 */
public class TextoDialogFragment extends DialogFragment {

    /** Claves del Bundle de respuesta. */
    public static final String RESULTADO_TEXTO = "texto";
    public static final String RESULTADO_TEXTO2 = "texto2";
    public static final String RESULTADO_ID = "id";

    private static final String ARG_CLAVE = "clave";
    private static final String ARG_TITULO = "titulo";
    private static final String ARG_HINT = "hint";
    private static final String ARG_HINT2 = "hint2";
    private static final String ARG_INICIAL = "inicial";
    private static final String ARG_CLAVES = "claves";
    private static final String ARG_ID = "id";

    /** Un campo de texto normal (nombre de una playlist). */
    public static void pedirTexto(FragmentManager fm, String clave, String titulo, String hint,
                                  String inicial, int id) {
        mostrar(fm, clave, titulo, hint, null, inicial, false, id);
    }

    /** Dos campos de contraseña (la actual y la nueva). */
    public static void pedirClaves(FragmentManager fm, String clave, String titulo,
                                   String hint, String hint2) {
        mostrar(fm, clave, titulo, hint, hint2, "", true, 0);
    }

    private static void mostrar(FragmentManager fm, String clave, String titulo, String hint,
                                String hint2, String inicial, boolean claves, int id) {
        Bundle args = new Bundle();
        args.putString(ARG_CLAVE, clave);
        args.putString(ARG_TITULO, titulo);
        args.putString(ARG_HINT, hint);
        args.putString(ARG_HINT2, hint2);
        args.putString(ARG_INICIAL, inicial);
        args.putBoolean(ARG_CLAVES, claves);
        args.putInt(ARG_ID, id);
        TextoDialogFragment dialogo = new TextoDialogFragment();
        dialogo.setArguments(args);
        dialogo.show(fm, clave);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        View vista = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_texto, null);
        TextInputLayout layout1 = vista.findViewById(R.id.dialogoLayout1);
        TextInputLayout layout2 = vista.findViewById(R.id.dialogoLayout2);
        TextInputEditText campo1 = vista.findViewById(R.id.dialogoCampo1);
        TextInputEditText campo2 = vista.findViewById(R.id.dialogoCampo2);

        boolean claves = args.getBoolean(ARG_CLAVES);
        layout1.setHint(args.getString(ARG_HINT));
        if (claves) {
            int tipo = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            campo1.setInputType(tipo);
            campo2.setInputType(tipo);
            layout1.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            layout2.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            layout2.setHint(args.getString(ARG_HINT2));
            layout2.setVisibility(View.VISIBLE);
        }
        if (savedInstanceState == null) {
            campo1.setText(args.getString(ARG_INICIAL));
            if (campo1.getText() != null) {
                campo1.setSelection(campo1.getText().length());
            }
        }

        AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(args.getString(ARG_TITULO))
                .setView(vista)
                .setPositiveButton(R.string.guardar, null) // se engancha abajo para poder validar
                .setNegativeButton(R.string.cancelar, null)
                .create();

        // Si el botón se configurara en el builder, el diálogo se cerraría siempre.
        // Así se puede dejar abierto mientras falte algún campo.
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(b -> {
                    String t1 = texto(campo1);
                    String t2 = texto(campo2);
                    boolean ok = true;
                    if (t1.isEmpty()) {
                        layout1.setError(getString(R.string.error_obligatorio));
                        ok = false;
                    }
                    if (claves && t2.isEmpty()) {
                        layout2.setError(getString(R.string.error_obligatorio));
                        ok = false;
                    }
                    if (!ok) {
                        return;
                    }
                    Bundle respuesta = new Bundle();
                    respuesta.putString(RESULTADO_TEXTO, t1);
                    respuesta.putString(RESULTADO_TEXTO2, t2);
                    respuesta.putInt(RESULTADO_ID, args.getInt(ARG_ID));
                    getParentFragmentManager().setFragmentResult(args.getString(ARG_CLAVE), respuesta);
                    dismiss();
                }));
        return dialogo;
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
