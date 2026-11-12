package es.ies.cm.dam2.pmdm.eco;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class CancionRepository {

    private final EcoDB db;

    public CancionRepository(Context context) {
        this.db = EcoDB.getInstance(context);
    }

    public List<Cancion> getCanciones() {
        List<Cancion> caniones = new ArrayList<>();

        try (Cursor cursor = db.getReadableDatabase().query(CancionContract.CancionEntry.TABLE_NAME, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                caniones.add(cursorToCancion(cursor));
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }

        return caniones;
    }

    public void insertCancion(Cancion cancion) {
        db.getWritableDatabase().insert(CancionContract.CancionEntry.TABLE_NAME, null, cantionToContentValues(cancion));
    }

    public void updateCancion(Cancion cancion) {

        db.getWritableDatabase().update(CancionContract.CancionEntry.TABLE_NAME, cantionToContentValues(cancion), CancionContract.CancionEntry.COLUMN_ID + " = ?", new String[]{String.valueOf(cancion.getId())});

    }

    public void deleteCancion(int id) {
        db.getWritableDatabase().delete(CancionContract.CancionEntry.TABLE_NAME, CancionContract.CancionEntry.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public boolean cancionExists(Cancion cancion) {
        try (Cursor cursor = db.getReadableDatabase().query(CancionContract.CancionEntry.TABLE_NAME, null, CancionContract.CancionEntry.COLUMN_ID + " = ?", new String[]{String.valueOf(cancion.getId())}, null, null, null)) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            return false;
        }
    }

    private ContentValues cantionToContentValues(Cancion cancion) {

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
