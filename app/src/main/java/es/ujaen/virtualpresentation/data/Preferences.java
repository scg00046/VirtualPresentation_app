package es.ujaen.virtualpresentation.data;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    /*
     * Método para guardar los datos del usuario en la aplicación
     *
     * Ejemplo de obtención de los datos
     * SharedPreferences sf = getPreferences(MODE_PRIVATE);
     *         String nombre = sf.getString("USER","");
     *         String expires = sf.getString("EXPIRES","");
     *         String sid = sf.getString("SID","");
     */

    /**
     *
     * @param context contexto de la aplicacion
     * @param u usuario con nombreusuario, nombre y apellidos
     */
    public static void saveCredentials(Context context, Usuario u){
        SharedPreferences sp = context.getSharedPreferences("default",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("nombreusuario",u.getNombreusuario());
        editor.putString("nombre",u.getNombre());
        editor.putString("apellidos",u.getApellidos());
        editor.commit();
    }
}
