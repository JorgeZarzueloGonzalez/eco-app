package es.ies.cm.dam2.pmdm.eco.data.room;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import es.ies.cm.dam2.pmdm.eco.model.Cancion;
import es.ies.cm.dam2.pmdm.eco.model.Reproduccion;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * P13 La base de datos Room de Eco: {@code eco_room.db}, versión 1, con
 * {@code canciones} y {@code reproducciones}.
 * <p>
 * <b>Singleton</b>: una única instancia para toda la app, creada con el
 * contexto de la aplicación (si se guardara una activity, se quedaría en
 * memoria aunque se cerrase).
 * <p>
 * <b>Executor</b>: Room prohíbe consultar en el hilo principal (bloquearía la
 * interfaz). Todas las llamadas a los DAO van en {@link #executor}, que tiene
 * un solo hilo: las tareas se ejecutan en el orden en que se encolan.
 * Para volver a tocar las vistas se usa {@code runOnUiThread(...)}.
 * <p>
 * No se usa {@code allowMainThreadQueries()} ni
 * {@code fallbackToDestructiveMigration()}: el primero congela la app y el
 * segundo borra los datos al cambiar de versión.
 */
@Database(entities = {Cancion.class, Reproduccion.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String NOMBRE_BD = "eco_room.db";

    /** Hilo de fondo para TODAS las operaciones de base de datos de la app. */
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static volatile AppDatabase instancia;

    public abstract CancionDao cancionDao();

    public abstract ReproduccionDao reproduccionDao();

    public static AppDatabase getInstancia(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    Context app = context.getApplicationContext();
                    instancia = Room.databaseBuilder(app, AppDatabase.class, NOMBRE_BD).build();

                    // Primera tarea del executor: la copia única desde eco.db.
                    // Como el hilo es único, cualquier consulta que llegue
                    // después espera a que termine.
                    executor.execute(() -> MigracionInicial.ejecutar(app, instancia));
                }
            }
        }
        return instancia;
    }
}
