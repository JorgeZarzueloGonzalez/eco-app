package es.ies.cm.dam2.pmdm.eco;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * P12 Acceso a la tabla {@code favoritos} de {@code eco.db}.
 * <p>
 * La tabla solo guarda {@code usuario_id} y {@code cancion_id}; para enseñar
 * el nombre y el género de la canción se resuelve cada identificador con
 * {@link CancionRepository#getCancion(int)} (sin JOIN, para que siga
 * funcionando cuando las canciones se muevan a Room en la P13).
 */
public class FavoritoRepository {

    private final EcoDB db;
    private final CancionRepository cancionRepository;

    public FavoritoRepository(Context context) {
        this.db = EcoDB.getInstance(context);
        this.cancionRepository = new CancionRepository(context);
    }

    /** Favoritos de un usuario, de la última canción añadida a la primera. */
    public List<Favorito> getFavoritos(int usuarioId) {
        List<Favorito> favoritos = new ArrayList<>();

        try (Cursor cursor = db.getReadableDatabase().query(
                FavoritoContract.FavoritoEntry.TABLE_NAME,
                null,
                FavoritoContract.FavoritoEntry.COLUMN_USUARIO_ID + " = ?",
                new String[]{String.valueOf(usuarioId)},
                null, null,
                FavoritoContract.FavoritoEntry.COLUMN_ID + " DESC")) {
            while (cursor.moveToNext()) {
                favoritos.add(cursorToFavorito(cursor));
            }
        }

        return favoritos;
    }

    /** El favorito concreto de un usuario y una canción, o {@code null}. */
    public Favorito getFavorito(int usuarioId, int cancionId) {
        try (Cursor cursor = db.getReadableDatabase().query(
                FavoritoContract.FavoritoEntry.TABLE_NAME,
                null,
                FavoritoContract.FavoritoEntry.COLUMN_USUARIO_ID + " = ? AND "
                        + FavoritoContract.FavoritoEntry.COLUMN_CANCION_ID + " = ?",
                new String[]{String.valueOf(usuarioId), String.valueOf(cancionId)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursorToFavorito(cursor);
            }
        }

        return null;
    }

    /** {@code true} si la canción ya está en los favoritos del usuario. */
    public boolean esFavorito(int usuarioId, int cancionId) {
        return getFavorito(usuarioId, cancionId) != null;
    }

    /**
     * Añade una canción a favoritos.
     * <p>
     * La tabla tiene {@code UNIQUE (usuario_id, cancion_id)}; si el favorito
     * ya existía, se ignora en vez de lanzar una excepción.
     *
     * @return el {@code _id} generado, o {@code -1} si ya existía.
     */
    public long insertFavorito(Favorito favorito) {
        return db.getWritableDatabase().insertWithOnConflict(
                FavoritoContract.FavoritoEntry.TABLE_NAME,
                null,
                favoritoToContentValues(favorito),
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Borra un favorito por su {@code _id}.
     *
     * @return el número de filas borradas (0 si el {@code id} no existe).
     */
    public int deleteFavorito(int id) {
        return db.getWritableDatabase().delete(
                FavoritoContract.FavoritoEntry.TABLE_NAME,
                FavoritoContract.FavoritoEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Quita una canción de los favoritos de un usuario.
     *
     * @return el número de filas borradas (0 si no era favorita).
     */
    public int deleteFavorito(int usuarioId, int cancionId) {
        return db.getWritableDatabase().delete(
                FavoritoContract.FavoritoEntry.TABLE_NAME,
                FavoritoContract.FavoritoEntry.COLUMN_USUARIO_ID + " = ? AND "
                        + FavoritoContract.FavoritoEntry.COLUMN_CANCION_ID + " = ?",
                new String[]{String.valueOf(usuarioId), String.valueOf(cancionId)});
    }

    /**
     * Canciones favoritas del usuario, ya con todos sus datos.
     * <p>
     * Si una canción se borró de la tabla {@code canciones} pero su favorito
     * sigue en la tabla, se descarta (puede pasar cuando las canciones se
     * muevan a Room y dejen de estar en {@code eco.db}).
     */
    public List<Cancion> getCancionesFavoritas(int usuarioId) {
        List<Cancion> canciones = new ArrayList<>();

        for (Favorito favorito : getFavoritos(usuarioId)) {
            Cancion cancion = cancionRepository.getCancion(favorito.getCancionId());
            if (cancion != null) {
                canciones.add(cancion);
            }
        }

        return canciones;
    }

    private ContentValues favoritoToContentValues(Favorito favorito) {
        ContentValues values = new ContentValues();

        values.put(FavoritoContract.FavoritoEntry.COLUMN_USUARIO_ID, favorito.getUsuarioId());
        values.put(FavoritoContract.FavoritoEntry.COLUMN_CANCION_ID, favorito.getCancionId());

        return values;
    }

    private Favorito cursorToFavorito(Cursor cursor) {
        Favorito favorito = new Favorito();
        favorito.setId(cursor.getInt(cursor.getColumnIndexOrThrow(FavoritoContract.FavoritoEntry.COLUMN_ID)));
        favorito.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(FavoritoContract.FavoritoEntry.COLUMN_USUARIO_ID)));
        favorito.setCancionId(cursor.getInt(cursor.getColumnIndexOrThrow(FavoritoContract.FavoritoEntry.COLUMN_CANCION_ID)));

        return favorito;
    }

}
