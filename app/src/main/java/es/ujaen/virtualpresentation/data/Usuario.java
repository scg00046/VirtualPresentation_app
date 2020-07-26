package es.ujaen.virtualpresentation.data;

import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Usuario {
    private String nombreusuario;
    private String nombre;
    private String apellidos;
    private List<Presentations> presentationsList;
    private List<String> lista;

    public Usuario(String nombreusuario, String nombre, String apellidos) {
        this.nombreusuario = nombreusuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.presentationsList = new ArrayList<>();
        this.lista = new ArrayList<>();
    }

    public Usuario(String user){
        String[] atributo = user.split(",");
        this.nombreusuario = atributo[0];
        this.nombre = atributo[1];
        this.apellidos = atributo[2];
        this.presentationsList = new ArrayList<>();
        this.lista = new ArrayList<>();
    }

    public Usuario() {
    }

    public String getNombreusuario() {
        return nombreusuario;
    }

    public void setNombreusuario(String nombreusuario) {
        this.nombreusuario = nombreusuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public List<Presentations> getPresentationsList() {
        return Collections.unmodifiableList(presentationsList);
    }

    public List<String> getLista() {
        return Collections.unmodifiableList(lista);
    }

    public void addPresentation(int id, String nombre){
        this.lista.add(nombre);
    }

    public void presentationsJSON (JSONArray array) throws JSONException{//TODO quitar log (hay demasiados)
        Log.i("Usuario_Json","Iniciando conversión");
            //JSONArray arrayJson = new JSONArray(cadenaJson);
            for (int i = 0; i < array.length(); i++){

                JSONObject pres = array.getJSONObject(i);
                int id = pres.getInt("idpresentacion");
                String presentacion = pres.getString("presentacion");
                Presentations presentation = new Presentations(id, presentacion,
                        pres.getInt("paginas") );
                Log.i("Usuario_Json",id+"-"+presentacion);
                this.presentationsList.add(/*id,*/presentation);
                this.lista.add(/*id,*/presentacion);

            }

    }

    public Presentations getPresentationByName (String presentacion){
        for (int i = 0; i<presentationsList.size(); i++){
            if (presentationsList.get(i).getPresentacion().equals(presentacion)){
                return presentationsList.get(i);
            }
        }
        return null;
    }
}
