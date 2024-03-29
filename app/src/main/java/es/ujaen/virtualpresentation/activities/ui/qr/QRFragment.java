package es.ujaen.virtualpresentation.activities.ui.qr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.activities.MainActivity;
import es.ujaen.virtualpresentation.activities.PresentationActivity;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Session;

public class QRFragment extends Fragment {

    private QRViewModel qrViewModel;

    private CameraSource cameraSource;
    private SurfaceView cameraView;

    private final int PERMISSIONS_REQUEST_CAMERA = 1;
    private String token = "";
    private String tokenanterior = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        qrViewModel =
                ViewModelProviders.of(this).get(QRViewModel.class);
        View root = inflater.inflate(R.layout.fragment_qr, container, false);
        final Context context = root.getContext();

        if (ActivityCompat.checkSelfPermission(root.getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) { //Permisos necesarios
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
            MainActivity.showFloatButton(); //Muestra el botón para actualizar manualmente el fragmento
        } else {
            MainActivity.hiddenFloatButton();
        }

        cameraView = (SurfaceView) root.findViewById(R.id.camera_view);
        initQR(context);
        return root;
    }

    @Override
    public void onDestroyView() {
        MainActivity.showFloatButton();
        super.onDestroyView();
    }

    public void initQR(final Context context) {
        // Detector de codigo QR
        BarcodeDetector barcodeDetector =
                new BarcodeDetector.Builder(context)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();
        // Abrir cámara
        cameraSource = new CameraSource
                .Builder(context, barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true)
                .build();

        // listener de ciclo de vida de la camara
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                //Verificar permisos
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) { //Permisos necesarios

                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CAMERA},
                                PERMISSIONS_REQUEST_CAMERA);
                    }
                    return;
                } else { //Permisos concedidos
                    MainActivity.hiddenFloatButton();
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException ie) {
                        Log.e("CamaraQR", ie.getMessage());
                    }
                }//fin else permisos
            }// fin surfaceCreated

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        // preparo el detector de QR
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0) {

                    // obtenemos el token
                    token = barcodes.valueAt(0).displayValue.toString();

                    // verificamos que el token anterior no se igual al actual
                    // esto es util para evitar multiples llamadas empleando el mismo token
                    if (!token.equals(tokenanterior)) {

                        // guardamos el ultimo token procesado
                        tokenanterior = token;

                        try {
                            JSONObject tokenJson = new JSONObject(token);
                            Session sqr = Session.sessionFromJSON(tokenJson); //Sesión del QR
                            Log.i("QR_sesionRecived", "Sesion:" + sqr.getNombreSesion() + " Pres: " + sqr.getPresentacion() + " User: " + sqr.getNombreUsuario());

                            Session sa = Preferences.getSession(context, sqr.getNombreSesion()); //Sesión almacenada
                            Log.i("QR_sesionSaved", "Sesion:" + sa.getNombreSesion() + " Pres: " + sa.getPresentacion() + " User: " + sa.getNombreUsuario());

                            //Comprueba nombre de usuario y presentación
                            if (sqr.getNombreUsuario().equals(sa.getNombreUsuario()) && sqr.getPresentacion().equals(sa.getPresentacion())) {
                                Log.i("QR_Sesion", "Sesión correcta");
                                String codigo = sqr.getCodigo();
                                nextActivity(context, sqr.getNombreSesion(), codigo);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    synchronized (this) {
                                        wait(5000);
                                        // limpiamos el token
                                        tokenanterior = "";
                                    }
                                } catch (InterruptedException e) {
                                    Log.e("Error", "Waiting didnt work!!");
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } //Fin if comprobación tocken nuevo
                } //Fin if tamaño de codigo >0
            } //Fin receiveDetections
        }); //Fin barcode.setProcessor
    }//Fin initQR

    /**
     * Realiza un intent para abrir la actividd de presentación, pasando el nombre de la sesión y el código
     *
     * @param context
     * @param nombreSesion
     * @param codigo
     */
    private void nextActivity(Context context, String nombreSesion, String codigo) {

        Intent intent = new Intent(context, PresentationActivity.class);
        intent.putExtra("sesion", nombreSesion);
        intent.putExtra("codigo", codigo);
        getContext().startActivity(intent);
        getActivity().finish(); //Finaliza la actividad principal
    }
}