package es.ies.cm.dam2.pmdm.eco;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EcoDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "eco.db";
    private static final int DB_VERSION = 1;
    private static EcoDB instance;

    private EcoDB(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static synchronized EcoDB getInstance(Context context) {
        if (instance == null) {
            instance = new EcoDB(context);
            return instance;
        } else {
            return instance;
        }
    }

    /**
     * Se ejecuta ANTES de onCreate/onUpgrade: aquí se activan las claves ajenas.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        super.onConfigure(db);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // ===== 1. Tablas =====
        // Primero las "padre" (canciones y usuarios): favoritos las referencia.
        db.execSQL("CREATE TABLE canciones (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "genero TEXT, " +
                "sello TEXT, " +
                "interpretes INTEGER, " +
                "anio INTEGER, " +
                "popularidad INTEGER, " +
                "duracion INTEGER, " +          // extra: el enunciado no la pide
                "ubicacion TEXT, " +
                "portada TEXT, " +
                "valoracion INTEGER" +
                ")");

        db.execSQL("CREATE TABLE usuarios (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL UNIQUE, " +
                "clave TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE favoritos (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER NOT NULL, " +
                "cancion_id INTEGER NOT NULL, " +
                "FOREIGN KEY (usuario_id) REFERENCES usuarios(_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (cancion_id) REFERENCES canciones(_id) ON DELETE CASCADE, " +
                "UNIQUE (usuario_id, cancion_id)" +
                ")");

        // ===== 2. Usuarios iniciales =====
        insertarUsuario(db, "admin", "admin");
        insertarUsuario(db, "carlos", "carlos");

        // ===== 3. Las 21 canciones =====
        insertarCancionesIniciales(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // ===================== ayudas =====================

    private void insertarUsuario(SQLiteDatabase db, String nombre, String clave) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("clave", clave);
        db.insert("usuarios", null, valores);
    }

    private void insertarCancionesIniciales(SQLiteDatabase db) {
        // La misma lista que tenías en el ArrayList de BibliotecaActivity.
        Cancion[] iniciales = {
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
        };

        for (Cancion c : iniciales) {
            ContentValues valores = new ContentValues();
            valores.put("nombre", c.getNombre());
            valores.put("genero", c.getGenero());
            valores.put("sello", c.getSello());
            valores.put("interpretes", c.getInterpretes());
            valores.put("anio", c.getAnio());
            valores.put("popularidad", c.getPopularidad());
            valores.put("duracion", c.getDuracion());
            valores.put("valoracion", c.getValoracion());
            // "ubicacion" y "portada" se quedan NULL: la portada se calcula
            // con Portadas.de(genero) y la canción no tiene ubicación física.
            db.insert("canciones", null, valores);
        }
    }
}
