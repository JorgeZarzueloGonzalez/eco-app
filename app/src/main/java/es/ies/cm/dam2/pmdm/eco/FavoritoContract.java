package es.ies.cm.dam2.pmdm.eco;

import android.provider.BaseColumns;

/**
 * Nombres de la tabla {@code favoritos} y de sus columnas, para no repetir
 * cadenas de texto en {@link EcoDB} ni en {@link FavoritoRepository}.
 */
public class FavoritoContract {

    private FavoritoContract() {
    }

    public static class FavoritoEntry implements BaseColumns {

        public static final String TABLE_NAME = "favoritos";

        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_USUARIO_ID = "usuario_id";
        public static final String COLUMN_CANCION_ID = "cancion_id";
    }

}
