package es.ies.cm.dam2.pmdm.eco;

import android.provider.BaseColumns;

public class CancionContract {

    private CancionContract() {
    }

    public static class CancionEntry implements BaseColumns {

        public static final String TABLE_NAME = "canciones";

        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_GENERO = "genero";
        public static final String COLUMN_SELLO = "sello";
        public static final String COLUMN_INTERPRETES = "interpretes";
        public static final String COLUMN_ANIO = "anio";
        public static final String COLUMN_POPULARIDAD = "popularidad";
        public static final String COLUMN_DURACION = "duracion";
        public static final String COLUMN_VALORACION = "valoracion";
    }

}
