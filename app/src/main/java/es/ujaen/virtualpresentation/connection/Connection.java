package es.ujaen.virtualpresentation.connection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Map;

import es.ujaen.virtualpresentation.activities.MainActivity;
import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.data.Constant;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Presentations;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.Usuario;

public class Connection {

    private final String ip_puerto = "192.168.1.10:8080";
    private final String dominio = "http://" + ip_puerto + "/virtualpresentation/";
    private final String URLusuario = dominio + "usuario";
    private final String URLsesion = dominio + "session/";
    private Context context;
    public Usuario user;

    public Connection(Context context) {
        Log.d("Connection", "Conexion creada");
        this.context = context;
    }

    public Connection(Context context, Usuario user) {
        Log.d("Connection", "Conexion creada");
        this.context = context;
        this.user = user;
    }

    public void login(final String usuario, final String password, final boolean guardar) {
        final int[] statusCode = new int[1];
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, Constant.URL_LOGIN,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        if (statusCode[0] == 200){
                            Log.i("LoginCode", "Codigo Estado 200");
                        }
                        Log.i("Login OK", "Se ha logeado correctamente. " + response);
                        Usuario u = null;
                        try {
                            u = new Usuario(new JSONObject(response));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (guardar) {
                            Preferences.saveCredentials(context, u);
                        }
                        Toast.makeText(context, "Autenticación correcta", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.e("ConnectionError", "Error al iniciar sesión" + error);
                if (error.networkResponse != null){ //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    Log.i("ConnectionError", "Codigo error http: "+error.networkResponse.statusCode);
                    switch (statusCode){
                        case 403:
                            Toast.makeText(context, "Usuario y/o contraseñas incorrectos", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(context, "Se ha producido un error en el servidor", Toast.LENGTH_SHORT).show();
                            break;
                        case 400:
                            Toast.makeText(context, "No se han enviado datos al servidor", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, "Error desconocido", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else { //Error al realizar la conexión
                    Log.i("ConnectionError", "No se puede conectar con el servidor");
                    Toast.makeText(context, "No se puede conectar con el servidor", Toast.LENGTH_SHORT).show();
                }

            }
        }) {
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

    /**
     * Obtiene las presentaciones almacenadas en el servidor
     * @param presList spinner para completarlo con las presentaciones recibidas
     */
    public void getPresentations(final Spinner presList) {
        final String URL = dominio + user.getNombreusuario();
        final int[] statusCode = new int[1];
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // En este apartado se programa lo que deseamos hacer en caso de no haber errores
                        Log.i("GetPresentaciones", "(" + statusCode[0] + ")" + response);
                        Toast.makeText(context, "Lista de presentaciones recibidas", Toast.LENGTH_LONG).show();

                        try {
                            user.presentationsJSON(new JSONArray(response));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("GetPresentacionesList", user.getLista().toString());
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                android.R.layout.simple_spinner_dropdown_item, user.getLista());
                        presList.setAdapter(adapter);
                        presList.setBackgroundColor(Color.WHITE);
                        Log.i("GetPresentacionesList", "Tamaño lista: " + user.getLista().size());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.w("ConnectionError", "Error al pedir las presentaciones, " + error.toString());
                Toast.makeText(context, "Error al recibir las presentaciones (" + statusCode[0] + ")", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                statusCode[0] = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
    /**
     * Método para crear una sesión en el servidor
     *
     * @param sesion
     * @param presentacion
     */
    public void crearSesion(final String sesion, final String presentacion, final View view) {
        StringRequest stringRequest;
        final int[] statusCode = new int[1];
        final String URL = URLsesion + user.getNombreusuario();
        stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {

                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {

                        Log.i("ConnectionSession", "(" + statusCode[0] + ") Se ha creado la sesión. " + response);
                        Presentations present = user.getPresentationByName(presentacion);
                        if (present != null) {
                            Session session = new Session(user.getNombreusuario(), sesion, presentacion, present.getPaginas());
                            Preferences.saveSession(context, session);
                            Snackbar.make(view, "Se ha creado la sesión", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(view, "No existe la presentación", Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(R.color.colorErrorBackg)
                                    .show();
                        }

                        //Toast.makeText(context, "Sesión creada", Toast.LENGTH_LONG).show();
                        //TODO cambiar de fragmento

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("ConnectionError", "Error al crear sesión" + error);
                Toast.makeText(context, "Error al crear sesión", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("session", sesion);
                parametros.put("presentation", presentacion);
                return parametros;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                statusCode[0] = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**
     * Método para obtener el hash MD5 a partir del usuario y la contraseña
     * https://www.yoelprogramador.com/como-encriptar-contrasenas-en-md5-en-java/
     *
     * @param usuario  nombre de usuario que se va a enviar
     * @param password contraseña a enviar
     * @return
     */
    private String getMD5(String usuario, String password) {
        String up = usuario + "-" + password;
        String hashtext;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(up.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            hashtext = number.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }
}
