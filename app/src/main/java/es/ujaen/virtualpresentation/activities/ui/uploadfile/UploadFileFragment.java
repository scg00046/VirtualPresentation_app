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

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final int READ_REQUEST_CODE = 42;

    private String presentacionStr;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("UploadFileFragment", "Iniciando Fragmento Upload file");
        mViewModel = ViewModelProviders.of(this).get(UploadFileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_upload, container, false);

        context = root.getContext();
        User user = Preferences.getUser(context);

        seleccionar = root.findViewById(R.id.up_selectfile);
        nombreFichero = root.findViewById(R.id.up_namefile);
        enviar = root.findViewById(R.id.up_sendFile);
        colorAccent = root.getResources().getColor(R.color.colorAccent, context.getTheme());
        colorGrey = root.getResources().getColor(R.color.colorGrey, context.getTheme());

        final UploadFile upload = new UploadFile(context, user);
        permisos(); //Solicita los permisos para leer la memoria
        activateSend(false);

        //Botón seleccionar
        seleccionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentacionStr = "";
                nombreFichero.setText("");
                activateSend(false);
                if (permisos()) {
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
                Log.i("UPLOAD", "onClick: present" + presentacionStr + " nombre fichero: " + nombreFichero.getText().toString());
                upload.subir(presentacionStr, nombreFichero.getText().toString().trim());
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(UploadFileViewModel.class);

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
                presentacionStr = data.getData().toString();
                activateSend(true);
            } else {
                nombreFichero.setText("No se ha seleccionado ningun fichero válido");
            }
        } //Fin if de código de respuesta
    }

    private String getFileName(final Uri uri) {
        String displayName = "";
        Cursor cursor = getActivity().getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() devuelve false si el cursor no tiene filas
            if (cursor != null && cursor.moveToFirst()) {

                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i("File", "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
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


    private boolean permisos() {
        boolean resultado;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) { //Permisos concedidos
            resultado = true;
        } else {
            // No se encuentran los permisos, deben solicitarse
            Toast.makeText(context, "Se necesitan permisos para acceder", Toast.LENGTH_SHORT).show();
            resultado = false;
            requestPermission();
        }
        return resultado;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_READ_EXTERNAL_STORAGE);

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * Activa o desactiva el botón para eliminar presentación
     *
     * @param activar
     */
    public static void activateSend(boolean activar) {
        enviar.setClickable(activar);
        enviar.setEnabled(activar);
        if (activar) {
            enviar.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
        } else {
            enviar.setBackgroundTintList(ColorStateList.valueOf(colorGrey));
        }
    }
}