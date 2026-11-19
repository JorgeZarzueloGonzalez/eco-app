package es.ies.cm.dam2.pmdm.eco.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import es.ies.cm.dam2.pmdm.eco.model.Playlist;
import es.ies.cm.dam2.pmdm.eco.model.Usuario;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code eco.db}: la base de datos SQLite de Eco (la {@code DamaBD} del enunciado).
 * <p>
 * Versiones:
 * <ul>
 *     <li><b>1</b> (P12): canciones, usuarios y favoritos.</li>
 *     <li><b>2</b> (P13): las canciones se van a Room; favoritos pierde su FK a canciones.</li>
 *     <li><b>3</b> (proyecto): los favoritos se convierten en <b>playlists</b>. Cada
 *     usuario puede tener varias, cada una con sus canciones.</li>
 * </ul>
 * Esquema actual:
 * <pre>
 * usuarios           (_id, nombre UNIQUE, clave)
 * playlists          (_id, usuario_id → usuarios ON DELETE CASCADE, nombre,
 *                     UNIQUE(usuario_id, nombre))
 * playlist_canciones (_id, playlist_id → playlists ON DELETE CASCADE, cancion_id,
 *                     UNIQUE(playlist_id, cancion_id))
 * </pre>
 * {@code cancion_id} apunta a {@code canciones.id} de Room ({@code eco_room.db}):
 * al ser otra base de datos no puede llevar clave ajena.
 * <p>
 * Todo el SQL de estas tablas está aquí. Las activities llaman a estos métodos
 * siempre desde {@code AppDatabase.executor}, nunca en el hilo principal.
 */
public class EcoDB extends SQLiteOpenHelper {

    public static final String DB_NAME = "eco.db";
    private static final int DB_VERSION = 3;

    private static final String T_USUARIOS = "usuarios";
    private static final String T_PLAYLISTS = "playlists";
    private static final String T_PLAYLIST_CANCIONES = "playlist_canciones";

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

    /**
     * Instalación limpia: la base de datos nace directamente en la versión 3,
     * con la carga inicial (2 usuarios y una playlist de ejemplo para cada uno).
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_USUARIOS + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL UNIQUE, "
                + "clave TEXT NOT NULL)");
        crearTablasPlaylists(db);

        long admin = insertarUsuario(db, "admin", "admin");
        long carlos = insertarUsuario(db, "carlos", "carlos");

        // Los ids de canción son los de la carga inicial de Room (1 = Luna de cobre,
        // 8 = Neón, 15 = Pulso, 16 = Circuito, 18 = Órbita), que en una instalación
        // limpia siempre se numeran del 1 al 21 en el mismo orden.
        long favoritos = insertarPlaylist(db, admin, "Favoritos");
        long correr = insertarPlaylist(db, carlos, "Para correr");
        insertarEnPlaylist(db, favoritos, 1);
        insertarEnPlaylist(db, favoritos, 8);
        insertarEnPlaylist(db, favoritos, 15);
        insertarEnPlaylist(db, correr, 15);
        insertarEnPlaylist(db, correr, 16);
        insertarEnPlaylist(db, correr, 18);
    }

    /**
     * Migraciones, en cadena: una base de datos v1 pasa por la 2 y luego por la 3.
     * <p>
     * La tabla {@code canciones} NO se borra aquí: Room la necesita para copiar
     * las canciones ({@code MigracionInicial}), que la borra al terminar.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // v1 -> v2: SQLite no deja quitar una FK con ALTER TABLE, así que
            // favoritos se reconstruye: renombrar, crear, copiar y borrar.
            db.execSQL("ALTER TABLE favoritos RENAME TO favoritos_v1");
            db.execSQL("CREATE TABLE favoritos ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "usuario_id INTEGER NOT NULL, "
                    + "cancion_id INTEGER NOT NULL, "
                    + "FOREIGN KEY (usuario_id) REFERENCES " + T_USUARIOS + "(_id) ON DELETE CASCADE, "
                    + "UNIQUE (usuario_id, cancion_id))");
            db.execSQL("INSERT INTO favoritos (_id, usuario_id, cancion_id) "
                    + "SELECT _id, usuario_id, cancion_id FROM favoritos_v1 "
                    + "WHERE usuario_id IN (SELECT _id FROM " + T_USUARIOS + ")");
            db.execSQL("DROP TABLE favoritos_v1");
        }
        if (oldVersion < 3) {
            // v2 -> v3: los favoritos de cada usuario pasan a una playlist "Favoritos".
            crearTablasPlaylists(db);
            db.execSQL("INSERT INTO " + T_PLAYLISTS + " (usuario_id, nombre) "
                    + "SELECT DISTINCT usuario_id, 'Favoritos' FROM favoritos");
            db.execSQL("INSERT INTO " + T_PLAYLIST_CANCIONES + " (playlist_id, cancion_id) "
                    + "SELECT p._id, f.cancion_id FROM favoritos f "
                    + "JOIN " + T_PLAYLISTS + " p ON p.usuario_id = f.usuario_id "
                    + "AND p.nombre = 'Favoritos'");
            db.execSQL("DROP TABLE favoritos");
        }
    }

    // ===================== usuarios (CRUD completo) =====================

    /** Registro (C): el {@code _id} nuevo, o {@code -1} si el nombre ya existe (UNIQUE). */
    public long registrar(String nombre, String clave) {
        ContentValues v = new ContentValues();
        v.put("nombre", nombre);
        v.put("clave", clave);
        return getWritableDatabase().insertWithOnConflict(T_USUARIOS, null, v,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    /** Login (R): el usuario con ese nombre (sin distinguir mayúsculas) y esa clave, o {@code null}. */
    public Usuario login(String nombre, String clave) {
        try (Cursor c = getReadableDatabase().query(T_USUARIOS, null,
                "nombre = ? COLLATE NOCASE AND clave = ?", new String[]{nombre, clave},
                null, null, null)) {
            return c.moveToFirst() ? leerUsuario(c) : null;
        }
    }

    /** (R) Un usuario por su {@code _id}, o {@code null} si no existe. */
    public Usuario getUsuario(int id) {
        try (Cursor c = getReadableDatabase().query(T_USUARIOS, null,
                "_id = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? leerUsuario(c) : null;
        }
    }

    /** (R) {@code true} si el nombre ya está cogido: el registro avisa antes de insertar. */
    public boolean existeUsuario(String nombre) {
        try (Cursor c = getReadableDatabase().query(T_USUARIOS, new String[]{"_id"},
                "nombre = ? COLLATE NOCASE", new String[]{nombre}, null, null, null)) {
            return c.moveToFirst();
        }
    }

    /**
     * Cambiar contraseña (U): solo si la actual es correcta.
     *
     * @return {@code true} si se ha cambiado.
     */
    public boolean cambiarClave(int id, String actual, String nueva) {
        ContentValues v = new ContentValues();
        v.put("clave", nueva);
        return getWritableDatabase().update(T_USUARIOS, v, "_id = ? AND clave = ?",
                new String[]{String.valueOf(id), actual}) == 1;
    }

    /** Borrar cuenta (D): sus playlists y sus canciones se van en cascada. */
    public void borrarUsuario(int id) {
        getWritableDatabase().delete(T_USUARIOS, "_id = ?", new String[]{String.valueOf(id)});
    }

    // ===================== playlists (CRUD completo) =====================

    /** (C) Nueva playlist: el {@code _id}, o {@code -1} si el usuario ya tiene una con ese nombre. */
    public long crearPlaylist(int usuarioId, String nombre) {
        return insertarPlaylist(getWritableDatabase(), usuarioId, nombre);
    }

    /**
     * (R) Las playlists de un usuario con su número de canciones.
     * El LEFT JOIN hace que salgan también las vacías (con 0).
     */
    public List<Playlist> getPlaylists(int usuarioId) {
        List<Playlist> lista = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT p._id, p.nombre, COUNT(pc._id) AS total "
                        + "FROM " + T_PLAYLISTS + " p "
                        + "LEFT JOIN " + T_PLAYLIST_CANCIONES + " pc ON pc.playlist_id = p._id "
                        + "WHERE p.usuario_id = ? "
                        + "GROUP BY p._id ORDER BY p.nombre COLLATE NOCASE",
                new String[]{String.valueOf(usuarioId)})) {
            while (c.moveToNext()) {
                Playlist p = new Playlist();
                p.setId(c.getInt(0));
                p.setNombre(c.getString(1));
                p.setNumCanciones(c.getInt(2));
                lista.add(p);
            }
        }
        return lista;
    }

    /** (R) El nombre de una playlist, o {@code null} si se ha borrado. */
    public String getNombrePlaylist(int playlistId) {
        try (Cursor c = getReadableDatabase().query(T_PLAYLISTS, new String[]{"nombre"},
                "_id = ?", new String[]{String.valueOf(playlistId)}, null, null, null)) {
            return c.moveToFirst() ? c.getString(0) : null;
        }
    }

    /** (U) Renombrar: {@code false} si el usuario ya tiene otra con ese nombre. */
    public boolean renombrarPlaylist(int playlistId, String nombre) {
        ContentValues v = new ContentValues();
        v.put("nombre", nombre);
        return getWritableDatabase().updateWithOnConflict(T_PLAYLISTS, v, "_id = ?",
                new String[]{String.valueOf(playlistId)}, SQLiteDatabase.CONFLICT_IGNORE) == 1;
    }

    /** (D) Borra la playlist; sus canciones se van en cascada. */
    public void borrarPlaylist(int playlistId) {
        getWritableDatabase().delete(T_PLAYLISTS, "_id = ?",
                new String[]{String.valueOf(playlistId)});
    }

    // ===================== canciones de una playlist =====================

    /** Los ids (de Room) de las canciones de la playlist, en el orden en que se añadieron. */
    public List<Integer> getCancionIds(int playlistId) {
        List<Integer> ids = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PLAYLIST_CANCIONES,
                new String[]{"cancion_id"}, "playlist_id = ?",
                new String[]{String.valueOf(playlistId)}, null, null, "_id")) {
            while (c.moveToNext()) {
                ids.add(c.getInt(0));
            }
        }
        return ids;
    }

    /** Añade una canción: {@code -1} si ya estaba (UNIQUE(playlist_id, cancion_id)). */
    public long anadirCancion(int playlistId, int cancionId) {
        return insertarEnPlaylist(getWritableDatabase(), playlistId, cancionId);
    }

    /** Quita una canción de la playlist (también se usa para limpiar huérfanas). */
    public void quitarCancion(int playlistId, int cancionId) {
        getWritableDatabase().delete(T_PLAYLIST_CANCIONES, "playlist_id = ? AND cancion_id = ?",
                new String[]{String.valueOf(playlistId), String.valueOf(cancionId)});
    }

    // ===================== ayudas =====================

    private void crearTablasPlaylists(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PLAYLISTS + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "usuario_id INTEGER NOT NULL, "
                + "nombre TEXT NOT NULL, "
                + "FOREIGN KEY (usuario_id) REFERENCES " + T_USUARIOS + "(_id) ON DELETE CASCADE, "
                + "UNIQUE (usuario_id, nombre))");
        db.execSQL("CREATE TABLE " + T_PLAYLIST_CANCIONES + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "playlist_id INTEGER NOT NULL, "
                + "cancion_id INTEGER NOT NULL, "
                + "FOREIGN KEY (playlist_id) REFERENCES " + T_PLAYLISTS + "(_id) ON DELETE CASCADE, "
                + "UNIQUE (playlist_id, cancion_id))");
    }

    private long insertarUsuario(SQLiteDatabase db, String nombre, String clave) {
        ContentValues v = new ContentValues();
        v.put("nombre", nombre);
        v.put("clave", clave);
        return db.insert(T_USUARIOS, null, v);
    }

    private long insertarPlaylist(SQLiteDatabase db, long usuarioId, String nombre) {
        ContentValues v = new ContentValues();
        v.put("usuario_id", usuarioId);
        v.put("nombre", nombre);
        return db.insertWithOnConflict(T_PLAYLISTS, null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private long insertarEnPlaylist(SQLiteDatabase db, long playlistId, int cancionId) {
        ContentValues v = new ContentValues();
        v.put("playlist_id", playlistId);
        v.put("cancion_id", cancionId);
        return db.insertWithOnConflict(T_PLAYLIST_CANCIONES, null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private Usuario leerUsuario(Cursor c) {
        Usuario u = new Usuario();
        u.setId(c.getInt(c.getColumnIndexOrThrow("_id")));
        u.setNombre(c.getString(c.getColumnIndexOrThrow("nombre")));
        return u;
    }
}
