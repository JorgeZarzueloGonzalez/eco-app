package es.ies.cm.dam2.pmdm.eco;

import android.provider.BaseColumns;

public class UsuarioContract {

    public UsuarioContract(){}

    public static class UsuarioEntry implements BaseColumns {

        public static final String TABLE_NAME = "usuarios";
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_CLAVE = "clave";

    }

}
