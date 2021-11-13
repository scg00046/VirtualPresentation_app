package es.ujaen.virtualpresentation.activities.ui.home;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.activities.MainActivity;
import es.ujaen.virtualpresentation.connection.Connection;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.User;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private EditText session;
    private static Spinner presList;
    private static Button enviaSesion;
    private TextView descipcion;
    private FragmentManager fm;

    private static int colorAccent;
    private static int colorGrey;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i("HomeFragment", "Iniciando Fragmento Home");
        Context context = HomeFragment.super.getContext();
        User user = Preferences.getUser(context);
        Log.i("HomeFragment_user", user.getNombreusuario() + " - " + user.getNombre());
        final Connection con = new Connection(context, user);
        fm = getActivity().getSupportFragmentManager();

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        final TextView textView = root.findViewById(R.id.text_home);
        session = root.findViewById(R.id.sessionname);
        presList = root.findViewById(R.id.presentationList);
        enviaSesion = root.findViewById(R.id.sendSession);
        descipcion = root.findViewById(R.id.sessionDescription);

        colorAccent = root.getResources().getColor(R.color.colorAccent, context.getTheme());
        colorGrey = root.getResources().getColor(R.color.colorGrey, context.getTheme());

        List<String> defSpinner = new ArrayList<>();
        defSpinner.add("No hay presentaciones para " + user.getNombreusuario());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,
                Collections.unmodifiableList(defSpinner));
        presList.setAdapter(adapter);
        activateSendSession(false);
        MainActivity.showHideLoading(context, true);
        con.getPresentations(presList, 0);

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        enviaSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sesion = session.getText().toString().trim();
                String seleccionado = presList.getSelectedItem().toString();
                if (sesion.isEmpty()) {
                    Toast.makeText(root.getContext().getApplicationContext(), "Introduzca el nombre de la sesión", Toast.LENGTH_LONG).show();
                } else {
                    con.createSession(sesion, seleccionado, root);
                }
            }
        });

        session.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    enviaSesion.setClickable(false);
                    enviaSesion.setEnabled(false);
                    enviaSesion.setBackgroundTintList(ColorStateList.valueOf(colorGrey));
                } else {
                    enviaSesion.setClickable(true);
                    enviaSesion.setEnabled(true);
                    enviaSesion.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return root;
    }

    /**
     * Activa o desactiva el botón para enviar sesión
     *
     * @param activar
     */
    public static void activateSendSession(boolean activar) {
        if (activar) {
            enviaSesion.setClickable(true);
            enviaSesion.setEnabled(true);
            presList.setClickable(true);
            enviaSesion.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
        } else {
            enviaSesion.setClickable(false);
            enviaSesion.setEnabled(false);
            presList.setClickable(false);
            enviaSesion.setBackgroundTintList(ColorStateList.valueOf(colorGrey));
        }
    }
}