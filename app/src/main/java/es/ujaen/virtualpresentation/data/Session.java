package es.ujaen.virtualpresentation.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Clase Session, parámetros de las sesiones creadas en la aplicación
 * @author Sergio Caballero Garrido
 */
public class Session {
    private String nombreUsuario;
    private String nombreSesion;
    private String presentacion;
    private int paginas;
    private int paginaInicio;
    private String codigo;

    public Session(String nombreUsuario, String nombreSesion, String presentacion, int paginas) {
        this.nombreUsuario = nombreUsuario;
        this.nombreSesion = nombreSesion;
        this.presentacion = presentacion;
        this.paginas = paginas;
    }

    public Session(String nombreUsuario, String nombreSesion, String presentacion, String codigo) {
        this.nombreUsuario = nombreUsuario;
        this.nombreSesion = nombreSesion;
        this.presentacion = presentacion;
        this.codigo = codigo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombreSesion() {
        return nombreSesion;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public int getPaginas() {
        return paginas;
    }

    public int getPaginaInicio() {
        return paginaInicio;
    }

    public void setPaginaInicio(int paginaInicio) {
        this.paginaInicio = paginaInicio;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getSesionCodigo(){
        return nombreSesion+"_"+codigo;
    }

    /**
     * Crea un objeto Session a partir de un objeto JSON recibido por el QR
     * @param s sesión en formato json
     * @return objeto session
     * @throws JSONException
     */
    public static Session sessionFromJSON(JSONObject s) throws JSONException {
        Log.i("Sesion_Json","Convirtiendo texto recibido");
        String nombreUsuario = s.getString("nombreusuario");
        String nombreSesion = s.getString("nombresesion");
        String presentacion = s.getString("presentacion");
        String codigo = s.getString("codigo");
        Session sesion = new Session(nombreUsuario,nombreSesion,presentacion, codigo);
        return sesion;
    }

}
