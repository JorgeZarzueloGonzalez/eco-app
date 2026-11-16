package es.ies.cm.dam2.pmdm.eco.data.contract;

import android.provider.BaseColumns;
import es.ies.cm.dam2.pmdm.eco.data.repository.FavoritoRepository;
import es.ies.cm.dam2.pmdm.eco.data.sqlite.EcoDB;

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
