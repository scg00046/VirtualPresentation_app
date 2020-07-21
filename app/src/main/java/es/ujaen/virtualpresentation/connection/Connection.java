package es.ujaen.virtualpresentation.connection;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Map;

import es.ujaen.virtualpresentation.MainActivity;
import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Usuario;

public class Connection {

    private final String ip_puerto = "192.168.1.10:8080";
    private final String dominio = "http://"+ip_puerto+"/virtualpresentation/";
    private final String URLusuario = dominio+"usuario";
    private Context context;

    public Connection(Context context) {
        Log.d("Connection","Conexion creada");
        this.context = context;
    }

    public void login(final String usuario, final String password, final boolean guardar) {
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, URLusuario,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) { //TODO recoger codigos de respuesta http
                        // En este apartado se programa lo que deseamos hacer en caso de no haber errores
                        if(response.startsWith("ERROR 1")) {
                            Toast.makeText(context, "Se deben de llenar todos los campos.", Toast.LENGTH_SHORT).show();
                        } else if(response.startsWith("ERROR 2")) {
                            Toast.makeText(context, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
                        } else if (response.startsWith("OK")){ //TODO recuperar objeto json
                            Log.i("Login OK","Se ha logeado correctamente. "+response);
                            //String[] resultado = response.split(":");
                            //resultado[1].split(",");
                            Usuario u = new Usuario(response.split(":")[1]);
                            if(guardar) {
                                Preferences.saveCredentials(context, u);
                            }
                            //Toast.makeText(context, "Inicio de Sesion exitoso.", Toast.LENGTH_LONG).show();
                            Toast.makeText(context, "Inicio OK"+response.split(":")[1], Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                //Toast.makeText(getApplicationContext(), "ERROR AL INICIAR SESION", Toast.LENGTH_LONG).show();
                Toast.makeText(context, "Error al iniciar sesión:"+error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                // En este metodo se hace el envio de valores de la aplicacion al servidor
                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("user", usuario);
                //parametros.put("password", getMD5(usuario,password));
                parametros.put("password", password);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    //Nueva petición
    public void getPresentations(final String usuario) {
        final String URL = dominio+usuario;
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) { //TODO recoger codigos de respuesta http
                        // En este apartado se programa lo que deseamos hacer en caso de no haber errores
                        Log.i("GetPresentaciones",response);
                        Toast.makeText(context, "Lista de presentaciones recibidas", Toast.LENGTH_LONG).show();

                        /*if(response.startsWith("ERROR 1")) {
                            Toast.makeText(context, "Se deben de llenar todos los campos.", Toast.LENGTH_SHORT).show();
                        } else if(response.startsWith("ERROR 2")) {
                            Toast.makeText(context, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
                        } else if (response.startsWith("OK")){
                            Log.i("Login OK","Se ha logeado correctamente. "+response);
                            //String[] resultado = response.split(":");
                            //resultado[1].split(",");
                            //Usuario u = new Usuario(response.split(":")[1]);

                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                        }*/

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                //Toast.makeText(getApplicationContext(), "ERROR AL INICIAR SESION", Toast.LENGTH_LONG).show();
                Toast.makeText(context, "Error al iniciar sesión:"+error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            /*@Override
            protected Map<String, String> getParams() throws AuthFailureError {

                // En este metodo se hace el envio de valores de la aplicacion al servidor
                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("user", usuario);
                //parametros.put("password", getMD5(usuario,password));
                parametros.put("password", password);

                return parametros;
            }*/
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
    //Fin nueva peitición

    /**
     * Método para obtener el hash MD5 a partir del usuario y la contraseña
     * https://www.yoelprogramador.com/como-encriptar-contrasenas-en-md5-en-java/
     * @param usuario nombre de usuario que se va a enviar
     * @param password contraseña a enviar
     * @return
     */
    private String getMD5(String usuario,String password){
        String up = usuario+"-"+password;
        String hashtext;
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(up.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            hashtext = number.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }
}
