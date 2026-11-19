package es.ies.cm.dam2.pmdm.eco.data.room;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import es.ies.cm.dam2.pmdm.eco.data.sqlite.EcoDB;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import es.ies.cm.dam2.pmdm.eco.model.Reproduccion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * P13 Carga inicial de {@code eco_room.db}, que se hace UNA sola vez.
 * <ul>
 *     <li><b>Hay canciones en {@code eco.db}</b> (la app venía de la P12): se
 *     copian a Room <b>con el mismo {@code id}</b>, para que la tabla
 *     {@code favoritos} siga apuntando a las mismas canciones. Después se
 *     borra la tabla vieja de {@code eco.db}.</li>
 *     <li><b>No hay nada que copiar</b> (instalación limpia): se siembran las
 *     21 canciones de ejemplo para que la biblioteca no arranque vacía.</li>
 * </ul>
 * Al terminar se guarda {@code migracion_canciones = true} en
 * {@code SharedPreferences}. Se marca DESPUÉS de copiar: si la copia fallara,
 * se reintentaría en el siguiente arranque. Y como no se repite nunca, una
 * canción borrada en Room no "resucita" desde la tabla vieja.
 * <p>
 * Siempre se ejecuta dentro de {@link AppDatabase#executor}.
 */
final class MigracionInicial {

    private static final String TAG = "MigracionInicial";
    private static final String PREFS = "eco_prefs";
    private static final String CLAVE_HECHA = "migracion_canciones";

    private MigracionInicial() {
    }

    static void ejecutar(Context context, AppDatabase room) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(CLAVE_HECHA, false)) {
            return;
        }

        try {
            List<Cancion> deSqlite = leerCancionesDeSqlite(context);

            if (!deSqlite.isEmpty()) {
                room.cancionDao().insertarTodas(deSqlite);
                Log.i(TAG, "Copiadas " + deSqlite.size() + " canciones de eco.db a Room");
            } else if (room.cancionDao().contar() == 0) {
                sembrar(room);
                Log.i(TAG, "Instalación limpia: sembrados los datos de ejemplo");
            }

            prefs.edit().putBoolean(CLAVE_HECHA, true).apply();

            // Solo cuando la copia ya está a salvo en Room.
            borrarTablaVieja(context);
        } catch (RuntimeException e) {
            // Sin marcar la bandera: se reintenta en el próximo arranque.
            Log.e(TAG, "La migración a Room ha fallado", e);
        }
    }

    /**
     * Lee la tabla {@code canciones} de {@code eco.db} si existe. Si el fichero
     * no existe no se abre (abrirlo lo crearía vacío) y no hay nada que copiar.
     */
    private static List<Cancion> leerCancionesDeSqlite(Context context) {
        List<Cancion> canciones = new ArrayList<>();
        if (!context.getDatabasePath(EcoDB.DB_NAME).exists()) {
            return canciones;
        }

        // Abrir EcoDB ejecuta antes su onUpgrade v1 -> v2 (favoritos sin FK a canciones).
        SQLiteDatabase db = EcoDB.getInstance(context).getWritableDatabase();
        if (!existeTabla(db, "canciones")) {
            return canciones;
        }

        try (Cursor c = db.rawQuery("SELECT * FROM canciones", null)) {
            while (c.moveToNext()) {
                Cancion cancion = new Cancion();
                cancion.setId(c.getInt(c.getColumnIndexOrThrow("_id"))); // ¡el mismo id!
                cancion.setNombre(c.getString(c.getColumnIndexOrThrow("nombre")));
                cancion.setGenero(c.getString(c.getColumnIndexOrThrow("genero")));
                cancion.setSello(c.getString(c.getColumnIndexOrThrow("sello")));
                cancion.setInterpretes(c.getInt(c.getColumnIndexOrThrow("interpretes")));
                cancion.setAnio(c.getInt(c.getColumnIndexOrThrow("anio")));
                cancion.setPopularidad(c.getInt(c.getColumnIndexOrThrow("popularidad")));
                cancion.setDuracion(c.getInt(c.getColumnIndexOrThrow("duracion")));
                cancion.setValoracion(c.getInt(c.getColumnIndexOrThrow("valoracion")));
                canciones.add(cancion);
            }
        }
        return canciones;
    }

    private static void borrarTablaVieja(Context context) {
        if (context.getDatabasePath(EcoDB.DB_NAME).exists()) {
            EcoDB.getInstance(context).getWritableDatabase()
                    .execSQL("DROP TABLE IF EXISTS canciones");
        }
    }

    private static boolean existeTabla(SQLiteDatabase db, String tabla) {
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?",
                new String[]{tabla})) {
            return c.moveToFirst();
        }
    }

    /**
     * Carga inicial del proyecto (instalación limpia): las 21 canciones de ejemplo,
     * algunas ya con estado y favorita, y tres reproducciones (la relación 1-N
     * que pide el enunciado). Los usuarios y la playlist de ejemplo los crea
     * {@code EcoDB.onCreate}.
     */
    private static void sembrar(AppDatabase room) {
        List<Cancion> canciones = cancionesDeEjemplo();
        marcar(canciones.get(0), Cancion.ESTADO_EN_BUCLE, true);   // Luna de cobre
        marcar(canciones.get(7), Cancion.ESTADO_ESCUCHADA, true);  // Neón
        marcar(canciones.get(14), Cancion.ESTADO_ESCUCHADA, false); // Pulso
        canciones.get(3).setExplicita(true);                        // Viento norte

        List<Long> ids = room.cancionDao().insertarTodas(canciones);

        long ahora = System.currentTimeMillis();
        long dia = 24L * 60 * 60 * 1000;
        int lunaDeCobre = ids.get(0).intValue();
        int neon = ids.get(7).intValue();
        room.reproduccionDao().insertar(new Reproduccion(lunaDeCobre, ahora - 2 * dia, 8, "admin"));
        room.reproduccionDao().insertar(new Reproduccion(lunaDeCobre, ahora - dia, 9, "carlos"));
        room.reproduccionDao().insertar(new Reproduccion(neon, ahora - 3 * dia, 7, "admin"));
    }

    private static void marcar(Cancion cancion, int estado, boolean favorita) {
        cancion.setEstado(estado);
        cancion.setFavorita(favorita);
    }

    /** Las 21 canciones que antes sembraba EcoDB (P12). */
    private static List<Cancion> cancionesDeEjemplo() {
        return Arrays.asList(
                new Cancion("Luna de cobre", "Rock", "Eco Records", 2, 2024, 8, 180, 7),
                new Cancion("Tormenta", "Rock", "Eco Records", 3, 2021, 7, 205, 6),
                new Cancion("Camino al sur", "Rock", "Sur Records", 1, 2019, 6, 165, 5),
                new Cancion("Viento norte", "Rock", "Sur Records", 4, 2023, 9, 190, 8),
                new Cancion("Semilla", "Rock", "Eco Records", 2, 2020, 5, 150, 4),
                new Cancion("Fuego lento", "Rock", "Sur Records", 2, 2022, 8, 175, 7),
                new Cancion("Arena", "Rock", "Eco Records", 3, 2018, 6, 200, 6),

                new Cancion("Neón", "Pop", "Luces Records", 1, 2025, 9, 210, 9),
                new Cancion("Mariposa", "Pop", "Luces Records", 1, 2023, 8, 195, 7),
                new Cancion("Ciudad dormida", "Pop", "Luces Records", 2, 2022, 7, 185, 6),
                new Cancion("Azúcar", "Pop", "Sol Records", 1, 2024, 9, 200, 8),
                new Cancion("Ventana", "Pop", "Sol Records", 2, 2021, 6, 170, 5),
                new Cancion("Domingo", "Pop", "Luces Records", 1, 2020, 7, 160, 6),
                new Cancion("Papel de plata", "Pop", "Sol Records", 4, 2019, 5, 155, 4),

                new Cancion("Pulso", "Electrónica", "Circuito Records", 1, 2025, 9, 240, 9),
                new Cancion("Circuito", "Electrónica", "Circuito Records", 2, 2024, 8, 225, 8),
                new Cancion("Amanecer binario", "Electrónica", "Nébula Records", 3, 2023, 7, 210, 7),
                new Cancion("Órbita", "Electrónica", "Nébula Records", 2, 2022, 8, 235, 8),
                new Cancion("Señal", "Electrónica", "Circuito Records", 1, 2021, 6, 205, 5),
                new Cancion("Bitácora", "Electrónica", "Nébula Records", 3, 2020, 7, 220, 6),
                new Cancion("Nébula", "Electrónica", "Nébula Records", 1, 2019, 6, 215, 5)
        );
    }
}
