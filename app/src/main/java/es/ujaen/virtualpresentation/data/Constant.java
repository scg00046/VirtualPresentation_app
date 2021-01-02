package es.ujaen.virtualpresentation.data;

import android.util.Log;
import androidx.annotation.NonNull;

/**
 * Clase Constant, constantes del servidor (urls)
 *
 * @author Sergio Caballero Garrido
 */
public class Constant {

    private static final String IP ="192.168.1.10";
    private static final String PORT ="8080";
    public static final String SERVER = "http://"+IP+":"+PORT;
    public static final String DIR_NAME = "/virtualpresentation";

    public static final String URL_LOGIN = SERVER+DIR_NAME+"/usuario";

    private static final String URL_USER = SERVER+DIR_NAME+"/";
    public static final String URL_SESSION = SERVER+DIR_NAME+"/session/";

    public static final String ROOM_SOCKET = "virtualPresentations";

    /**
     * Obtiene la url completa para el usuario
     * @param usuario autenticado
     * @return url '../virtualpresentation/{usuario}'
     */
    public static String getUrlUser(@NonNull String usuario) {
        Log.i("Constant", "URL usuario: "+URL_USER+usuario);
        return URL_USER+usuario;
    }

    /**
     * Obtiene la url completa para las sesiones del usuario
     * @param usuario autenticado
     * @return url '../virtualpresentation/session/{usuario}'
     */
    public static String getUrlSessionUser(@NonNull String usuario) {
        Log.i("Constant", "URL sesion usuario: "+URL_SESSION+usuario);
        return URL_SESSION+usuario;
    }

}
