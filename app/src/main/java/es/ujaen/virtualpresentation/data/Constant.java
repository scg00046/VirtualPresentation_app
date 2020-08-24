package es.ujaen.virtualpresentation.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

public class Constant {

    private static final String IP ="192.168.1.10";
    private static final String PORT ="8080";
    public static final String SERVER = "http://"+IP+":"+PORT;
    public static final String DIR_NAME = "/virtualpresentation";

    public static final String URL_LOGIN = SERVER+DIR_NAME+"/usuario";

    private static final String URL_USER = SERVER+DIR_NAME+"/";
    //public static final String IP = "";
    //public static final String IP = "";

    /**
     * Obtiene la url completa para el usuario
     * @param usuario
     * @return
     */
    public static String getUrlUser(@NonNull String usuario) {
        Log.i("Constant", "URL usuario: "+URL_USER+usuario);
        return URL_USER+usuario;
    }
}
