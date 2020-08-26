package es.ujaen.virtualpresentation.data;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    /**
     *  Guarda los datos del usuario en preferencias compartidas
     *  'default'
     * @param context contexto de la aplicacion
     * @param u usuario con nombreusuario, nombre y apellidos
     */
    public static void saveCredentials(Context context, User u, boolean guardar){
        SharedPreferences sp = context.getSharedPreferences("default", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("id",u.getId());
        editor.putString("nombreusuario",u.getNombreusuario());
        editor.putString("nombre",u.getNombre());
        editor.putString("apellidos",u.getApellidos());
        editor.putBoolean("permanente", guardar);
        editor.commit();
    }

    /**
     * Si no se marca la opción de recordar credenciales se eliminan al cerrar la aplicación
     * @param context
     */
    public static void deleteCredentials(Context context) {
        SharedPreferences sp = context.getSharedPreferences("default", Context.MODE_PRIVATE);
        boolean guardar = sp.getBoolean("permanente", true);
        if (!guardar) { //Si las credenciales son temporales
            SharedPreferences.Editor editor = sp.edit();
            editor.clear().commit();
        }
    }

    /**
     * Obtiene de las preferencias compartidas el usuario logueado
     * SharedPreferences 'default'
     * @param context
     * @return user usuario logueado
     */
    public static User getUser(Context context){
        SharedPreferences sp = context.getSharedPreferences("default",Context.MODE_PRIVATE);
        int id = sp.getInt("id",0);
        String usuario = sp.getString("nombreusuario", "");
        String nombre = sp.getString("nombre", "");
        String apellidos = sp.getString("apellidos", "");
        User user = new User(id, usuario, nombre, apellidos);
        return user;
    }

    /**
     * Guarda los datos de sesión
     * @param context
     * @param session
     */
    public static void saveSession(Context context, Session session){
        SharedPreferences sp = context.getSharedPreferences(session.getNombreSesion(),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("nombreusuario",session.getNombreUsuario());
        editor.putString("presentacion", session.getPresentacion());
        editor.putInt("paginas", session.getPaginas());
        editor.commit();
    }

    /**
     * Obtiene la sesión dado el nombre de la misma
     * @param context
     * @param nombreSesion
     * @return
     */
    public static Session getSession(Context context, String nombreSesion){
        SharedPreferences sp = context.getSharedPreferences(nombreSesion,Context.MODE_PRIVATE);
        String usuario = sp.getString("nombreusuario", "");
        String presentacion = sp.getString("presentacion", "");
        int paginas = sp.getInt("paginas",0);
        Session session = new Session(usuario, nombreSesion,presentacion,paginas);
        return session;
    }

    /**
     * Elimina una sesión creada
     * @param context
     * @param nombreSesion
     */
    public static void deleteSession(Context context, String nombreSesion) {
        SharedPreferences sp = context.getSharedPreferences(nombreSesion, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear().commit();
    }
}
