package es.ujaen.virtualpresentation.connection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import es.ujaen.virtualpresentation.activities.MainActivity;
import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.activities.ui.delete.DeleteFragment;
import es.ujaen.virtualpresentation.activities.ui.home.HomeFragment;
import es.ujaen.virtualpresentation.data.Constant;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Presentations;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.User;

/**
 * Clase Connection, realiza peticiones HTTP al servidor
 *
 * @author Sergio Caballero Garrido
 */
public class Connection {

    private Context context;
    private User user;

    /**
     * Constructor con el contexto, para el login
     *
     * @param context contexto de la aplicación
     */
    public Connection(Context context) {
        Log.d("Connection", "Conexion creada");
        this.context = context;
    }

    /**
     * Constructor con el contexto y usuario
     * Para obtener lista presentaciones, crear sesión y eliminar presentación
     *
     * @param context contexto de la aplicación
     * @param user    usuario autenticado
     */
    public Connection(Context context, User user) {
        Log.d("Connection", "Conexion creada");
        this.context = context;
        this.user = user;
    }

    /**
     * Autenticación con el servidor
     *
     * @param usuario
     * @param password
     * @param guardar
     */
    public void login(final String usuario, final String password, final boolean guardar) {
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, Constant.URL_LOGIN,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.i("Login OK", "Se ha logeado correctamente. " + response);
                        User u;
                        try {
                            u = new User(new JSONObject(response));
                            Preferences.saveCredentials(context, u, guardar);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(context, "Autenticación correcta", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, MainActivity.class); //Pasa al activity principal
                        context.startActivity(intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Si se recibe un código de error
                Log.e("ConnectionError", "Error al iniciar sesión" + error);
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    Log.i("ConnectionError", "Codigo error http: " + error.networkResponse.statusCode);
                    switch (statusCode) {
                        case 400:
                            Toast.makeText(context, "No se han enviado datos al servidor", Toast.LENGTH_SHORT).show();
                            break;
                        case 401:
                            Toast.makeText(context, "Nombre de usuario y/o contraseñas incorrectos", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(context, "Se ha producido un error en el servidor", Toast.LENGTH_SHORT).show();
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
                parametros.put("password", password);
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**
     * Obtiene la lista de presentaciones almacenadas en el servidor
     *
     * @param presList spinner para completarlo con las presentaciones recibidas
     */
    public void getPresentations(final Spinner presList, final int fragmento) {
        final int[] statusCode = new int[1];
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.getUrlUser(user.getNombreusuario()),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) { //Respuesta sin errores
                        Log.i("GetPresentaciones", "(" + statusCode[0] + ")" + response);
                        //Toast.makeText(context, "Lista de presentaciones recibidas", Toast.LENGTH_LONG).show();
                        try {
                            user.presentationsJSONtoList(new JSONArray(response));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("GetPresentacionesList", user.getListaPresentacionesString().toString());
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                android.R.layout.simple_spinner_dropdown_item, user.getListaPresentacionesString());
                        presList.setAdapter(adapter);
                        if (fragmento == 0) {
                            HomeFragment.activateSendSession(true);
                        } else if (fragmento == 1){
                            DeleteFragment.activateDelete(true);
                        }
                        Log.i("GetPresentacionesList", "Tamaño lista: " + user.getListaPresentacionesString().size());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.w("ConnectionError", "Error al pedir las presentaciones, " + error.toString());
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    if (statusCode == 401) {
                        Toast.makeText(context, "No hay presentaciones para el usuario", Toast.LENGTH_SHORT).show();
                    } else if (statusCode == 500) {
                        Toast.makeText(context, "Error en el servidor", Toast.LENGTH_SHORT).show();
                    }
                } else { //Error en la conexión
                    Toast.makeText(context, "Error en la conexión", Toast.LENGTH_SHORT).show();
                }
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
     * @param sesion       nombre de la sesión a crear
     * @param presentacion nombre de la presentación
     * @param view         vista para usar snackbar
     */
    public void createSession(final String sesion, final String presentacion, final View view) {
        StringRequest stringRequest;
        final int[] statusCode = new int[1];
        stringRequest = new StringRequest(Request.Method.POST, Constant.getUrlSessionUser(user.getNombreusuario()),
                new Response.Listener<String>() {

                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {

                        Log.i("ConnectionSession", "(" + statusCode[0] + ") Se ha creado la sesión. " + response);
                        if (savePreferencesSession(sesion,presentacion,view)){
                            Snackbar.make(view, "Se ha creado la sesión", Snackbar.LENGTH_LONG).show();
                        }
                        //TODO cambiar de fragmento
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("ConnectionError", "Error al crear sesión" + error);
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    switch (statusCode) {
                        case 400: //La sesión ya existe
                            Log.w("CreateSessionError", "La sesión ya existe en el servidor.");
                            if (savePreferencesSession(sesion,presentacion,view)) {
                                Snackbar.make(view, "Se ha guardado la sesión", Snackbar.LENGTH_LONG).show();
                            }
                            break;
                        case 301: //Actualización de la sesión
                            Log.w("CreateSessionError", "La sesión ya existe en el servidor con otra presentación. Se ha actualizado");
                            if (savePreferencesSession(sesion,presentacion,view)) {
                                Snackbar.make(view, "Se ha actualizado la sesión a otra presentación", Snackbar.LENGTH_LONG).show();
                            }
                            break;
                        case 406: //No se han recibido datos en el servidor
                            Snackbar.make(view, "No se han enviado datos", Snackbar.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }//Fin switch
                }
            }//Fin on error response
        }) {
            @Override
            protected Map<String, String> getParams() {//Parámetros de la petición

                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("session", sesion);
                parametros.put("presentation", presentacion);
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    @SuppressLint("ResourceAsColor")
    private boolean savePreferencesSession (String sesion, String presentacion, View view){
        boolean resultado;
        Presentations present = user.getPresentationByName(presentacion);
        if (present != null) { //TODO revisar si es necesario
            Session session = new Session(user.getNombreusuario(), sesion, presentacion, present.getPaginas());
            Preferences.saveSession(context, session);
            TextView descripcion = (TextView) view.findViewById(R.id.sessionDescription);
            String url = Constant.getUrlUser(user.getNombreusuario())+"/"+sesion;
            descripcion.setText(url);
            resultado = true;
        } else {
            Snackbar.make(view, "No existe la presentación", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(R.color.colorErrorBackg)
                    .show();
            resultado = false;
        }
        return resultado;
    }

    /**
     * Método para borrar una presentación del servidor
     *
     * @param presentacion nombre de la presentación a eliminar
     */
    public void deletePresentation(final String presentacion) {
        Log.i("DeleteParams", "Presentacion: " + presentacion + " id usuario: " + String.valueOf(user.getId()));
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, Constant.getUrlUser(user.getNombreusuario()),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.i("DeletePresentation", "Se ha eliminado correctamente. " + response);
                        Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.e("ConnectionError", "Error al iniciar sesión" + error);
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    Log.i("ConnectionError", "Codigo error http: " + statusCode);
                    switch (statusCode) {
                        case 403: //Usuario no válido
                            Toast.makeText(context, "User no registrado", Toast.LENGTH_SHORT).show();
                            break;
                        case 406: //No se han recibido datos en el servidor
                            Toast.makeText(context, "Faltan parámetros en la petición", Toast.LENGTH_SHORT).show();
                            break;
                        case 500: //No se ha podido eliminar del servidor
                            Toast.makeText(context,
                                    "Se ha producido un error en el servidor y no se ha eliminado la presentación",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case 503: //No se ha podido eliminar de la bbdd
                            Toast.makeText(context, "No se ha podido eliminar en la base de datos", Toast.LENGTH_SHORT).show();
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
            protected Map<String, String> getParams() throws AuthFailureError { //TODO no envía los parámetros (Revisión)-PROBAR
                // En este metodo se hace el envio de valores de la aplicacion al servidor
                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("presentacion", presentacion);
                parametros.put("id", String.valueOf(user.getId()));
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**
     * Elimina una sesión creada del servidor
     * @param sesion
     */
    public void deleteSession (final String sesion){
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE,
                Constant.getUrlSessionUser(user.getNombreusuario()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, "Se ha eliminado la sesión", Toast.LENGTH_SHORT).show();
                        Log.i("DeleteSession", "Se ha eliminado correctamente. " + response);
                        Preferences.deleteSession(context, sesion);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.e("ConnectionError", "Error al iniciar sesión" + error);
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    Log.i("ConnectionError", "Codigo error http: " + statusCode);
                }else { //Error al realizar la conexión
                    Log.i("ConnectionError", "No se puede conectar con el servidor");
                    Toast.makeText(context, "No se puede conectar con el servidor", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            /*@Override
            protected Map<String, String> getParams() throws AuthFailureError {

                //
                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("session", sesion);
                parametros.put("id", String.valueOf(user.getId()));
                return parametros;
                //return super.getParams();
            }*/

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //return super.getHeaders();
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json"); //TODO probar 05/10
                headers.put("session", sesion);
                headers.put("id", String.valueOf(user.getId()) );
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}
