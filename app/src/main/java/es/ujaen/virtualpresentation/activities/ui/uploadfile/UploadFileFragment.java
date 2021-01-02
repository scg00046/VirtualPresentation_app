package es.ujaen.virtualpresentation.activities.ui.uploadfile;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.UploadFile;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.User;

public class UploadFileFragment extends Fragment {

    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private UploadFileViewModel mViewModel;

    private TextView nombreFichero;
    private Button seleccionar;
    private static Button enviar;

    private static int colorAccent;
    private static int colorGrey;

    private Context context;
    //private Activity activity;

    private static final int READ_REQUEST_CODE = 42;

    private Uri presentacionUri;

    /*public static UploadFileFragment newInstance() {
        return new UploadFileFragment();
    }*/

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("UploadFileFragment", "Iniciando Fragmento Upload file");
        mViewModel = ViewModelProviders.of(this).get(UploadFileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_upload, container, false);

        seleccionar = root.findViewById(R.id.up_selectfile);
        nombreFichero = root.findViewById(R.id.up_namefile);
        enviar = root.findViewById(R.id.up_sendFile);
        colorAccent = root.getResources().getColor(R.color.colorAccent);
        colorGrey = root.getResources().getColor(R.color.colorGrey);

        context = root.getContext();
        User user = Preferences.getUser(context);

        final UploadFile upload = new UploadFile(context, user);
        permisos(); //Solicita los permisos para leer la memoria
        activateSend(false);

        //Botón seleccionar
        seleccionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentacionUri = Uri.EMPTY;
                nombreFichero.setText("");
                activateSend(false);
                if(permisos()) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/pdf");
                    startActivityForResult(Intent.createChooser(intent, "Seleccionar presentación"), READ_REQUEST_CODE);
                }
            }
        });
        //Botón enviar
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload.subir(presentacionUri, nombreFichero.getText().toString());
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(UploadFileViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String filename = getFileName(uri);
                nombreFichero.setVisibility(View.VISIBLE);
                nombreFichero.setText(filename);
                if (uri.getAuthority().startsWith("com.android.externalstorage")) { //Ruta de directorios

                    String[] path = uri.getPath().split(":");
                    presentacionUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + File.separator + path[1]);
                    Log.i("SelectFile", "Uri original: "+uri+"\nuri modificada: "+presentacionUri);
                    activateSend(true);
                } else if (uri.getAuthority().startsWith("com.android.providers")) { //Ruta de caché
                    nombreFichero.setText("Seleccione el fichero desde el directorio, no desde caché");
                }
            } else {
                nombreFichero.setText("No se ha seleccionado ningun fichero válido");
            }
        } //Fin if de código de respuesta
    }

    private String getFileName(final Uri uri) {
        String displayName="";
        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = getActivity().getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i("File", "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i("File", "Size: " + size);
            }
        } finally {
            cursor.close();
        }
        return displayName;
    }

    //https://github.com/android/permissions-samples/blob/9e7afc19c202dd63d829b131dfe5fd9d8033f46b/RuntimePermissionsBasic/Application/src/main/java/com/example/android/basicpermissions/MainActivity.java
    private boolean permisos() {
        boolean resultado;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) { //Permisos concedidos
            resultado = true;
            //startCamera();
        } else {
            // Permission is missing and must be requested.
            Toast.makeText(context, "Se necesitan permisos para acceder", Toast.LENGTH_SHORT).show();
            resultado = false;
            requestPermission();
        }
        return resultado;
    }

    private void requestPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_READ_EXTERNAL_STORAGE);

        } else {
            //Snackbar.make(mLayout, R.string.camera_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * Activa o desactiva el botón para eliminar presentación
     * @param activar
     */
    public static void activateSend(boolean activar){
        enviar.setClickable(activar);
        enviar.setEnabled(activar);
        if (activar){
            enviar.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
        }else {
            enviar.setBackgroundTintList(ColorStateList.valueOf(colorGrey));
        }
    }
}