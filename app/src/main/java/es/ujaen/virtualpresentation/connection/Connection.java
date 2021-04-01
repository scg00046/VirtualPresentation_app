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
import java.util.List;
import java.util.Map;

import es.ujaen.virtualpresentation.activities.LoginActivity;
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
                    if (statusCode == 401) {
                        Toast.makeText(context, "Nombre de usuario y/o contraseñas incorrectos", Toast.LENGTH_SHORT).show();
                    } else {
                        responseCode(statusCode);
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
                        if (statusCode[0] == 200) {
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
                            } else if (fragmento == 1) {
                                DeleteFragment.activateDelete(true);
                            }
                            Log.i("GetPresentacionesList", "Tamaño lista: " + user.getListaPresentacionesString().size());
                        } else {
                            Toast.makeText(context, "No hay presentaciones para el usuario", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.w("ConnectionError", "Error al pedir las presentaciones, " + error.toString());
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    responseCode(statusCode);
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

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, user.getToken());
                return headers;
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
                        String mensaje = "";
                        switch (response) {
                            case "Creada":
                                mensaje = "Se ha creado la sesión";
                                break;
                            case "Sin cambios":
                                mensaje = "Se ha guardado la sesión";
                                break;
                            case "Actualizada":
                                mensaje = "Se ha actualizado la sesión con otra presentación";
                                break;
                        }
                        if (savePreferencesSession(sesion, presentacion, view)) {
                            Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("ConnectionError", "Error al crear sesión" + error);
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    responseCode(statusCode);
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

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, user.getToken());
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    @SuppressLint("ResourceAsColor")
    private boolean savePreferencesSession(String sesion, String presentacion, View view) {
        boolean resultado;
        Presentations present = user.getPresentationByName(presentacion);
        if (present != null) { //TODO revisar si es necesario
            Session session = new Session(user.getNombreusuario(), sesion, presentacion, present.getPaginas());
            Preferences.saveSession(context, session);
            TextView descripcion = (TextView) view.findViewById(R.id.sessionDescription);
            String url = Constant.getUrlUser(user.getNombreusuario()) + "/" + sesion;
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
    public void deletePresentation(final String presentacion, final Spinner presList) {
        Log.i("DeleteParams", "Presentacion: " + presentacion);
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.DELETE, Constant.getUrlUser(user.getNombreusuario()),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.i("DeletePresentation", "Se ha eliminado correctamente. " + response);
                        Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                        List<String> pres = user.getListaPresentacionesString();
                        pres.remove(presentacion);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                android.R.layout.simple_spinner_dropdown_item, pres);
                        presList.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.e("ConnectionError", "Error al iniciar sesión" + error);
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    Log.i("ConnectionError", "Codigo error http: " + statusCode);
                    responseCode(statusCode);
                } else { //Error al realizar la conexión
                    Log.i("ConnectionError", "No se puede conectar con el servidor");
                    Toast.makeText(context, "No se puede conectar con el servidor", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, user.getToken());
                headers.put("presentacion", presentacion);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**
     * Elimina una sesión creada del servidor
     *
     * @param sesion
     */
    public void deleteSession(final String sesion) {
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
                    responseCode(statusCode);
                } else { //Error al realizar la conexión
                    Log.i("ConnectionError", "No se puede conectar con el servidor");
                    Toast.makeText(context, "No se puede conectar con el servidor", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //return super.getHeaders();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, user.getToken());
                headers.put("session", sesion);
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void createUser(final User u, final String pass, final String mail) {
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, Constant.URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("OK")) {
                            Toast.makeText(context, "Usuario registrado", Toast.LENGTH_SHORT).show();
                            Log.i("CreateUser", "Se ha registrado correctamente. " + response);
                            LoginActivity.stopLoading();
                            LoginActivity.setUser(u.getNombreusuario());
                        }

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.e("ConnectionError", "Error al iniciar sesión" + error);
                LoginActivity.stopLoading();
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    Log.i("ConnectionError", "Codigo error http: " + statusCode);
                    responseCode(statusCode);
                } else { //Error al realizar la conexión
                    Log.i("ConnectionError", "No se puede conectar con el servidor");
                    Toast.makeText(context, "No se puede conectar con el servidor", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("nombreusuario", u.getNombreusuario());
                parametros.put("password", pass);
                parametros.put("nombre", u.getNombre());
                parametros.put("apellidos", u.getApellidos());
                parametros.put("email", mail);
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void logout () {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { //Respuesta sin errores
                        Log.i("LogOut", "Respuesta: " + response);
                        Toast.makeText(context, "Sesión de usuario cerrada", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.w("ConnectionError", "Error al pedir las presentaciones, " + error.toString());
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                } else { //Error en la conexión
                    Toast.makeText(context, "Error en la conexión", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, user.getToken());
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    private void responseCode (int statusCode) {
        switch (statusCode) {
            case 400:
                Toast.makeText(context, "No se han enviado los datos necesarios", Toast.LENGTH_SHORT).show();
                break;
            case 401:
                Toast.makeText(context, "Autenticación incorrecta, vuelva a iniciar sesión", Toast.LENGTH_SHORT).show();
                logout();
                Preferences.deleteCredentials(context);
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                break;
            case 403:
                Toast.makeText(context, "Los datos enviados son incorrectos o incompletos", Toast.LENGTH_SHORT).show();
                break;
            case 409:
                Toast.makeText(context, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                break;
            case 500:
                Toast.makeText(context, "Error en el servidor", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(context, "Error desconocido", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
