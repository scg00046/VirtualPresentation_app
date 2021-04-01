package es.ujaen.virtualpresentation.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase usuario, parámetros del usuario que ha accedido a la aplicación
 * @author Sergio Caballero Garrido
 */
public class User {

    private int id;
    private String nombreusuario;
    private String nombre;
    private String apellidos;
    private String mail;
    private String token;
    //TODO revisar listas
    private List<Presentations> listaPresentaciones; //Almacena los datos de las presentaciones
    private List<String> listaPresentacionesString; //Usada para el spinner

    /**
     * Constructor con parámetros
     * @param id
     * @param nombreusuario
     * @param nombre
     * @param apellidos
     */
    public User(int id, String nombreusuario, String nombre, String apellidos, String token) {
        this.id = id;
        this.nombreusuario = nombreusuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.token = token;
        this.listaPresentaciones = new ArrayList<>();
        this.listaPresentacionesString = new ArrayList<>();
    }

    /**
     * Constructor User a partir de un JSON recibido
     * @param userJson
     * @throws JSONException
     */
    public User(JSONObject userJson) throws JSONException{
        this.id = userJson.getInt("id");
        this.nombreusuario = userJson.getString("nombreusuario");
        this.nombre = userJson.getString("nombre");
        this.apellidos = userJson.getString("apellidos");
        this.token = userJson.getString("token");
        this.listaPresentaciones = new ArrayList<>();
        this.listaPresentacionesString = new ArrayList<>();
    }

    public User() {
    }

    public User(String nombreusuario, String nombre, String apellidos) {
        this.nombreusuario = nombreusuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
    }

    public int getId() {
        return id;
    }

    public String getNombreusuario() {
        return nombreusuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public List<Presentations> getListaPresentaciones() {
        return Collections.unmodifiableList(listaPresentaciones);
    }

    public List<String> getListaPresentacionesString() {
        return listaPresentacionesString;
    }

    public void addPresentation(int id, String nombre){
        this.listaPresentacionesString.add(nombre);
    }

    public String getToken() {
        return token;
    }

    public void presentationsJSONtoList(JSONArray array) throws JSONException{
        Log.i("Usuario_Json","Iniciando conversión");
            for (int i = 0; i < array.length(); i++){
                JSONObject pres = array.getJSONObject(i);
                int id = pres.getInt("idpresentacion");
                String nombrePresentacion = pres.getString("presentacion");
                Presentations presentacion = new Presentations(id, nombrePresentacion,
                        pres.getInt("paginas") );
                Log.i("Usuario_Json",id+"-"+nombrePresentacion);
                this.listaPresentaciones.add(presentacion);
                this.listaPresentacionesString.add(nombrePresentacion);
            }
    }

    public Presentations getPresentationByName (String presentacion){
        for (int i = 0; i< listaPresentaciones.size(); i++){
            if (listaPresentaciones.get(i).getPresentacion().equals(presentacion)){
                return listaPresentaciones.get(i);
            }
        }
        return null;
    }
}
