package es.ies.cm.dam2.pmdm.eco;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * P12 Acceso a la tabla {@code usuarios} de {@code eco.db}.
 * <p>
 * Todo el SQL de la tabla vive aquí: las actividades no deben construir
 * consultas ni tocar {@link EcoDB} directamente.
 */
public class UsuarioRepository {

    private final EcoDB db;

    public UsuarioRepository(Context context) {
        this.db = EcoDB.getInstance(context);
    }

    /** Todos los usuarios, ordenados por nombre. */
    public List<Usuario> getUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();

        try (Cursor cursor = db.getReadableDatabase().query(
                UsuarioContract.UsuarioEntry.TABLE_NAME,
                null, null, null, null, null,
                UsuarioContract.UsuarioEntry.COLUMN_NOMBRE + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                usuarios.add(cursorToUsuario(cursor));
            }
        }

        return usuarios;
    }

    /** Un usuario por su {@code _id}, o {@code null} si no existe. */
    public Usuario getUsuario(int id) {
        try (Cursor cursor = db.getReadableDatabase().query(
                UsuarioContract.UsuarioEntry.TABLE_NAME,
                null,
                UsuarioContract.UsuarioEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursorToUsuario(cursor);
            }
        }

        return null;
    }

    /** Un usuario por su nombre (sin distinguir mayúsculas), o {@code null}. */
    public Usuario getUsuarioPorNombre(String nombre) {
        try (Cursor cursor = db.getReadableDatabase().query(
                UsuarioContract.UsuarioEntry.TABLE_NAME,
                null,
                UsuarioContract.UsuarioEntry.COLUMN_NOMBRE + " = ? COLLATE NOCASE",
                new String[]{nombre},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursorToUsuario(cursor);
            }
        }

        return null;
    }

    /** Login: {@code true} si existe un usuario con ese nombre y esa clave. */
    public boolean validarCredenciales(String nombre, String clave) {
        try (Cursor cursor = db.getReadableDatabase().query(
                UsuarioContract.UsuarioEntry.TABLE_NAME,
                new String[]{UsuarioContract.UsuarioEntry.COLUMN_ID},
                UsuarioContract.UsuarioEntry.COLUMN_NOMBRE + " = ? COLLATE NOCASE AND "
                        + UsuarioContract.UsuarioEntry.COLUMN_CLAVE + " = ?",
                new String[]{nombre, clave},
                null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    /** Registro: {@code true} si el nombre ya está ocupado (la columna es UNIQUE). */
    public boolean existeNombre(String nombre) {
        try (Cursor cursor = db.getReadableDatabase().query(
                UsuarioContract.UsuarioEntry.TABLE_NAME,
                new String[]{UsuarioContract.UsuarioEntry.COLUMN_ID},
                UsuarioContract.UsuarioEntry.COLUMN_NOMBRE + " = ? COLLATE NOCASE",
                new String[]{nombre},
                null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    /**
     * Inserta un usuario.
     *
     * @return el {@code _id} generado, o {@code -1} si falla (por ejemplo,
     * nombre repetido).
     */
    public long insertUsuario(Usuario usuario) {
        return db.getWritableDatabase().insert(
                UsuarioContract.UsuarioEntry.TABLE_NAME, null,
                usuarioToContentValues(usuario));
    }

    /**
     * Actualiza los datos del usuario.
     *
     * @return el número de filas modificadas (0 si el {@code id} no existe).
     */
    public int updateUsuario(Usuario usuario) {
        return db.getWritableDatabase().update(
                UsuarioContract.UsuarioEntry.TABLE_NAME,
                usuarioToContentValues(usuario),
                UsuarioContract.UsuarioEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(usuario.getId())});
    }

    /**
     * Borra el usuario. Sus favoritos se borran en cascada por la clave ajena
     * de {@code favoritos}.
     *
     * @return el número de filas borradas (0 si el {@code id} no existe).
     */
    public int deleteUsuario(int id) {
        return db.getWritableDatabase().delete(
                UsuarioContract.UsuarioEntry.TABLE_NAME,
                UsuarioContract.UsuarioEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private ContentValues usuarioToContentValues(Usuario usuario) {
        ContentValues values = new ContentValues();

        values.put(UsuarioContract.UsuarioEntry.COLUMN_NOMBRE, usuario.getNombre());
        values.put(UsuarioContract.UsuarioEntry.COLUMN_CLAVE, usuario.getClave());

        return values;
    }

    private Usuario cursorToUsuario(Cursor cursor) {
        Usuario usuario = new Usuario();
        usuario.setId(cursor.getInt(cursor.getColumnIndexOrThrow(UsuarioContract.UsuarioEntry.COLUMN_ID)));
        usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(UsuarioContract.UsuarioEntry.COLUMN_NOMBRE)));
        usuario.setClave(cursor.getString(cursor.getColumnIndexOrThrow(UsuarioContract.UsuarioEntry.COLUMN_CLAVE)));

        return usuario;
    }

}
