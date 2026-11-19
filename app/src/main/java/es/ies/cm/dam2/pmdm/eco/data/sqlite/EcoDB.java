package es.ies.cm.dam2.pmdm.eco.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import es.ies.cm.dam2.pmdm.eco.model.Favorito;
import es.ies.cm.dam2.pmdm.eco.model.Usuario;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code eco.db}: la base de datos SQLite de Eco (la {@code DamaBD} del enunciado).
 * <p>
 * P12 (versión 1): canciones, usuarios y favoritos.<br>
 * P13 (versión 2): solo <b>usuarios</b> y <b>favoritos</b>. Las canciones viven
 * en Room ({@code eco_room.db}), así que {@code favoritos.cancion_id} ya no
 * tiene clave ajena: apunta a una tabla de OTRA base de datos.
 * <p>
 * Aquí está todo el SQL de las dos tablas: las activities llaman a estos
 * métodos (siempre desde {@code AppDatabase.executor}) y no escriben SQL.
 */
public class EcoDB extends SQLiteOpenHelper {

    public static final String DB_NAME = "eco.db";
    private static final int DB_VERSION = 2;

    private static final String T_USUARIOS = "usuarios";
    private static final String T_FAVORITOS = "favoritos";

    private static EcoDB instancia;

    private EcoDB(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static synchronized EcoDB getInstance(Context context) {
        if (instancia == null) {
            instancia = new EcoDB(context);
        }
        return instancia;
    }

    /** Se ejecuta ANTES de onCreate/onUpgrade: SQLite no comprueba las FK si no se activan. */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** Instalación limpia: la base de datos nace directamente en la versión 2. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_USUARIOS + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL UNIQUE, "
                + "clave TEXT NOT NULL)");

        crearTablaFavoritos(db);

        insertarUsuario(db, "admin", "admin");
        insertarUsuario(db, "carlos", "carlos");
    }

    /**
     * P13 v1 -> v2: SQLite no deja quitar una clave ajena con ALTER TABLE, así
     * que {@code favoritos} se reconstruye: renombrar, crear, copiar y borrar.
     * <p>
     * La tabla {@code canciones} NO se borra aquí: Room la necesita para
     * copiar las canciones ({@code MigracionInicial}), que la borra al terminar.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + T_FAVORITOS + " RENAME TO favoritos_v1");
            crearTablaFavoritos(db);
            db.execSQL("INSERT INTO " + T_FAVORITOS + " (_id, usuario_id, cancion_id) "
                    + "SELECT _id, usuario_id, cancion_id FROM favoritos_v1 "
                    + "WHERE usuario_id IN (SELECT _id FROM " + T_USUARIOS + ")");
            db.execSQL("DROP TABLE favoritos_v1");
        }
    }

    // ===================== usuarios =====================

    /** Login: el usuario con ese nombre (sin distinguir mayúsculas) y esa clave, o {@code null}. */
    public Usuario login(String nombre, String clave) {
        try (Cursor c = getReadableDatabase().query(T_USUARIOS, null,
                "nombre = ? COLLATE NOCASE AND clave = ?", new String[]{nombre, clave},
                null, null, null)) {
            return c.moveToFirst() ? leerUsuario(c) : null;
        }
    }

    /** Un usuario por su {@code _id}, o {@code null} si no existe. */
    public Usuario getUsuario(int id) {
        try (Cursor c = getReadableDatabase().query(T_USUARIOS, null,
                "_id = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? leerUsuario(c) : null;
        }
    }

    // ===================== favoritos =====================

    /** Favoritos de un usuario, del último añadido al primero. */
    public List<Favorito> getFavoritos(int usuarioId) {
        List<Favorito> favoritos = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_FAVORITOS, null,
                "usuario_id = ?", new String[]{String.valueOf(usuarioId)},
                null, null, "_id DESC")) {
            while (c.moveToNext()) {
                Favorito f = new Favorito();
                f.setId(c.getInt(c.getColumnIndexOrThrow("_id")));
                f.setCancionId(c.getInt(c.getColumnIndexOrThrow("cancion_id")));
                favoritos.add(f);
            }
        }
        return favoritos;
    }

    /**
     * Añade una canción a los favoritos del usuario. Por el
     * {@code UNIQUE(usuario_id, cancion_id)} un duplicado se ignora.
     *
     * @return el {@code _id} nuevo, o {@code -1} si ya era favorita.
     */
    public long insertarFavorito(int usuarioId, int cancionId) {
        ContentValues valores = new ContentValues();
        valores.put("usuario_id", usuarioId);
        valores.put("cancion_id", cancionId);
        return getWritableDatabase().insertWithOnConflict(T_FAVORITOS, null, valores,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    /** Quita una canción de los favoritos de un usuario. */
    public void borrarFavorito(int usuarioId, int cancionId) {
        getWritableDatabase().delete(T_FAVORITOS, "usuario_id = ? AND cancion_id = ?",
                new String[]{String.valueOf(usuarioId), String.valueOf(cancionId)});
    }

    /** Borra un favorito por su {@code _id} (los huérfanos de canciones borradas). */
    public void borrarFavorito(int id) {
        getWritableDatabase().delete(T_FAVORITOS, "_id = ?", new String[]{String.valueOf(id)});
    }

    // ===================== ayudas =====================

    /** Esquema v2: FK solo a usuarios (CASCADE) y sin duplicados. */
    private void crearTablaFavoritos(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_FAVORITOS + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "usuario_id INTEGER NOT NULL, "
                + "cancion_id INTEGER NOT NULL, "
                + "FOREIGN KEY (usuario_id) REFERENCES " + T_USUARIOS + "(_id) ON DELETE CASCADE, "
                + "UNIQUE (usuario_id, cancion_id))");
    }

    private void insertarUsuario(SQLiteDatabase db, String nombre, String clave) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("clave", clave);
        db.insert(T_USUARIOS, null, valores);
    }

    private Usuario leerUsuario(Cursor c) {
        Usuario u = new Usuario();
        u.setId(c.getInt(c.getColumnIndexOrThrow("_id")));
        u.setNombre(c.getString(c.getColumnIndexOrThrow("nombre")));
        return u;
    }
}
