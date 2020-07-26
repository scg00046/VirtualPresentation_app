package es.ujaen.virtualpresentation.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.channels.SeekableByteChannel;

public class Preferences {

    /**
     *  Guarda los datos del usuario en preferencias compartidas
     *  'default'
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

    /**
     * Obtiene de las preferencias compartidas el usuario logueado
     * SharedPreferences 'default'
     * @param context
     * @return user usuario logueado
     */
    public static Usuario obtenerUsuario(Context context){
        SharedPreferences sp = context.getSharedPreferences("default",Context.MODE_PRIVATE);
        String usuario = sp.getString("nombreusuario", "");
        String nombre = sp.getString("nombre", "");
        String apellidos = sp.getString("apellidos", "");
        Usuario user = new Usuario(usuario,nombre,apellidos);
        return user;
    }

    public static void saveSession(Context context, Session session){
        SharedPreferences sp = context.getSharedPreferences(session.getNombreSesion(),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("nombreusuario",session.getNombreUsuario());
        editor.putString("presentacion", session.getPresentacion());
        editor.putInt("paginas", session.getPaginas());
        editor.commit();
    }

    public static Session obtenerSession (Context context, String nombreSesion){
        SharedPreferences sp = context.getSharedPreferences(nombreSesion,Context.MODE_PRIVATE);
        String usuario = sp.getString("nombreusuario", "");
        String presentacion = sp.getString("presentacion", "");
        int paginas = sp.getInt("paginas",0);
        Session session = new Session(usuario, nombreSesion,presentacion,paginas);
        return session;
    }

    public static void deleteSession(Context context, String nombreSesion) {
        SharedPreferences sp = context.getSharedPreferences(nombreSesion, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear().commit();
    }
}
