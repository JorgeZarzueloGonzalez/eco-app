package es.ies.cm.dam2.pmdm.eco.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import es.ies.cm.dam2.pmdm.eco.data.contract.CancionContract;
import es.ies.cm.dam2.pmdm.eco.data.sqlite.EcoDB;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import java.util.ArrayList;
import java.util.List;

/**
 * P12 Acceso a la tabla {@code canciones} de {@code eco.db}.
 * <p>
 * Toda la biblioteca (alta, ficha, búsqueda, edición y borrado) pasa por aquí.
 * Borrar una canción borra en cascada sus favoritos ({@code ON DELETE CASCADE}).
 */
public class CancionRepository {

    private final EcoDB db;

    public CancionRepository(Context context) {
        this.db = EcoDB.getInstance(context);
    }

    /** Todas las canciones, ordenadas por nombre. */
    public List<Cancion> getCanciones() {
        List<Cancion> canciones = new ArrayList<>();

        try (Cursor cursor = db.getReadableDatabase().query(
                CancionContract.CancionEntry.TABLE_NAME,
                null, null, null, null, null,
                CancionContract.CancionEntry.COLUMN_NOMBRE + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                canciones.add(cursorToCancion(cursor));
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }

        return canciones;
    }

    /** Una canción por su {@code _id}, o {@code null} si no existe. */
    public Cancion getCancion(int id) {
        try (Cursor cursor = db.getReadableDatabase().query(
                CancionContract.CancionEntry.TABLE_NAME,
                null,
                CancionContract.CancionEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursorToCancion(cursor);
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    /**
     * Buscador de la P12: filtra por nombre (empieza por o contiene) y por
     * valoración mínima.
     *
     * @param texto           lo escrito por el usuario ({@code ""} = todas)
     * @param empiezaPor      {@code true}: {@code texto%}; {@code false}: {@code %texto%}
     * @param valoracionMinima 0 para no filtrar por valoración
     */
    public List<Cancion> buscar(String texto, boolean empiezaPor, int valoracionMinima) {
        List<Cancion> canciones = new ArrayList<>();

        String patron = patron(texto, empiezaPor);
        String seleccion = CancionContract.CancionEntry.COLUMN_NOMBRE + " LIKE ? ESCAPE '\\'"
                + " AND " + CancionContract.CancionEntry.COLUMN_VALORACION + " >= ?";

        try (Cursor cursor = db.getReadableDatabase().query(
                CancionContract.CancionEntry.TABLE_NAME,
                null,
                seleccion,
                new String[]{patron, String.valueOf(valoracionMinima)},
                null, null,
                CancionContract.CancionEntry.COLUMN_NOMBRE + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                canciones.add(cursorToCancion(cursor));
            }
        }

        return canciones;
    }

    /**
     * Inserta una canción y le asigna el {@code _id} que genera SQLite.
     *
     * @return el {@code _id} generado, o {@code -1} si falla.
     */
    public int insertCancion(Cancion cancion) {
        long id = db.getWritableDatabase().insert(
                CancionContract.CancionEntry.TABLE_NAME, null,
                cancionToContentValues(cancion));

        cancion.setId((int) id);
        return (int) id;
    }

    /**
     * Actualiza la fila de la canción.
     *
     * @return el número de filas modificadas (0 si el {@code id} no existe).
     */
    public int updateCancion(Cancion cancion) {
        return db.getWritableDatabase().update(
                CancionContract.CancionEntry.TABLE_NAME,
                cancionToContentValues(cancion),
                CancionContract.CancionEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(cancion.getId())});
    }

    /**
     * Borra la canción por su {@code _id}. Sus favoritos se van en cascada.
     *
     * @return el número de filas borradas (0 si el {@code id} no existe).
     */
    public int deleteCancion(int id) {
        return db.getWritableDatabase().delete(
                CancionContract.CancionEntry.TABLE_NAME,
                CancionContract.CancionEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /** {@code true} si existe una canción con ese {@code _id}. */
    public boolean cancionExists(Cancion cancion) {
        try (Cursor cursor = db.getReadableDatabase().query(
                CancionContract.CancionEntry.TABLE_NAME,
                new String[]{CancionContract.CancionEntry.COLUMN_ID},
                CancionContract.CancionEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(cancion.getId())},
                null, null, null)) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            return false;
        }
    }

    /** Construye el patrón de {@code LIKE}, escapando los comodines que escriba el usuario. */
    private String patron(String texto, boolean empiezaPor) {
        String limpio = (texto == null ? "" : texto.trim())
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        return empiezaPor ? limpio + "%" : "%" + limpio + "%";
    }

    private ContentValues cancionToContentValues(Cancion cancion) {
        ContentValues values = new ContentValues();

        values.put(CancionContract.CancionEntry.COLUMN_NOMBRE, cancion.getNombre());
        values.put(CancionContract.CancionEntry.COLUMN_GENERO, cancion.getGenero());
        values.put(CancionContract.CancionEntry.COLUMN_SELLO, cancion.getSello());
        values.put(CancionContract.CancionEntry.COLUMN_INTERPRETES, cancion.getInterpretes());
        values.put(CancionContract.CancionEntry.COLUMN_ANIO, cancion.getAnio());
        values.put(CancionContract.CancionEntry.COLUMN_POPULARIDAD, cancion.getPopularidad());
        values.put(CancionContract.CancionEntry.COLUMN_DURACION, cancion.getDuracion());
        values.put(CancionContract.CancionEntry.COLUMN_VALORACION, cancion.getValoracion());

        return values;
    }

    private Cancion cursorToCancion(Cursor cursor) {
        Cancion cancion = new Cancion();
        cancion.setId(cursor.getInt(cursor.getColumnIndexOrThrow(CancionContract.CancionEntry.COLUMN_ID)));
        cancion.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(CancionContract.CancionEntry.COLUMN_NOMBRE)));
        cancion.setGenero(cursor.getString(cursor.getColumnIndexOrThrow(CancionContract.CancionEntry.COLUMN_GENERO)));
        cancion.setSello(cursor.getString(cursor.getColumnIndexOrThrow(CancionContract.CancionEntry.COLUMN_SELLO)));
        cancion.setInterpretes(cursor.getInt(cursor.getColumnIndexOrThrow(CancionContract.CancionEntry.COLUMN_INTERPRETES)));
        cancion.setAnio(cursor.getInt(cursor.getColumnIndexOrThrow(CancionContract.CancionEntry.COLUMN_ANIO)));
        cancion.setPopularidad(cursor.getInt(cursor.getColumnIndexOrThrow(CancionContract.CancionEntry.COLUMN_POPULARIDAD)));
        cancion.setDuracion(cursor.getInt(cursor.getColumnIndexOrThrow(CancionContract.CancionEntry.COLUMN_DURACION)));
        cancion.setValoracion(cursor.getInt(cursor.getColumnIndexOrThrow(CancionContract.CancionEntry.COLUMN_VALORACION)));

        return cancion;
    }

}
