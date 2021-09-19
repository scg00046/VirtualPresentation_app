package es.ujaen.virtualpresentation.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Clase Preferences, datos almacenados en la aplicación
 * @author Sergio Caballero Garrido
 */
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
        editor.putString("token", u.getToken());
        editor.commit();
    }

    /**
     * Elimina las credenciales
     * @param context
     */
    public static void deleteCredentials(Context context) {
        SharedPreferences sp = context.getSharedPreferences("default", Context.MODE_PRIVATE);
        boolean guardar = sp.getBoolean("permanente", true);
        sp.edit().clear().commit();
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
        String token = sp.getString("token", "");
        User user = new User(id, usuario, nombre, apellidos, token);
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
        editor.putInt("paginaInicial", 1);
        editor.commit();
        addSessionList(context, session.getNombreSesion());
    }

    /**
     * Actualiza los datos de sesión
     * @param context
     * @param session
     */
    public static void refreshSession(Context context, Session session, int paginafinal){
        SharedPreferences sp = context.getSharedPreferences(session.getNombreSesion(),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("nombreusuario",session.getNombreUsuario());
        editor.putString("presentacion", session.getPresentacion());
        editor.putInt("paginas", session.getPaginas());
        editor.putInt("paginaInicial", paginafinal);
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
        int paginaInicial = sp.getInt("paginaInicial", 1);
        Session session = new Session(usuario, nombreSesion,presentacion,paginas);
        session.setPaginaInicio(paginaInicial);
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
        removeSessionList(context, nombreSesion);
    }

    public static boolean exitsSession (Context context, String nombreSesion) {
        SharedPreferences sp = context.getSharedPreferences(nombreSesion, Context.MODE_PRIVATE);
        String presentacion = sp.getString("presentacion", "");
        if (presentacion.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    private static void addSessionList (Context context, String session) {
        SharedPreferences sp = context.getSharedPreferences("all_Sessions",Context.MODE_PRIVATE);
        Set<String> sesiones = sp.getStringSet("sesiones", new HashSet<String>());
        sesiones.add(session);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet("sesiones",sesiones);
        editor.commit();
    }

    private static void removeSessionList(Context context, String session) {
        SharedPreferences sp = context.getSharedPreferences("all_Sessions",Context.MODE_PRIVATE);
        Set<String> sesiones = sp.getStringSet("sesiones", new HashSet<String>());
        sesiones.remove(session);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet("sesiones",sesiones);
        editor.commit();
    }

    public static void removeAllSession(Context context) {
        SharedPreferences sp = context.getSharedPreferences("all_Sessions", Context.MODE_PRIVATE);
        Set<String> sesiones = sp.getStringSet("sesiones", new HashSet<String>());
        Object[] list = sesiones.toArray();
        for (int i = 0; i < list.length; i++) {
            deleteSession(context, list[i].toString());
        }
    }
}
