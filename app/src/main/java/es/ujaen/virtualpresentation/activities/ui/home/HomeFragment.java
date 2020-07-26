package es.ujaen.virtualpresentation.activities.ui.home;

import android.content.Context;
import android.os.Bundle;
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
import java.util.List;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.Connection;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Usuario;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private EditText session;
    private Spinner presList;
    private Button enviaSesion;
    private FragmentManager fm;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i("HomeFragment", "arrancando Fragmento Home");
        Context context = HomeFragment.super.getContext();
        Usuario user = Preferences.obtenerUsuario(context);
        final Connection con = new Connection(context, user);
        fm = getActivity().getSupportFragmentManager();

        List<String> present = new ArrayList<>();// = con.user.getLista();
        Log.i("HomeFragment_user", user.getNombreusuario()+" - "+user.getNombre());
        //Log.i("HomeFragment_list", present.toString());

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        final TextView textView = root.findViewById(R.id.text_home);
        session = root.findViewById(R.id.sessionname);
        presList = root.findViewById(R.id.presentationList);
        enviaSesion = root.findViewById(R.id.sendSession);


        present.add("No hay presentaciones para " + user.getNombreusuario());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(root.getContext().getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, present);
        presList.setAdapter(adapter);


        //final Connection con = new Connection(root.getContext().getApplicationContext(), user);
        con.getPresentations(presList);
        /*if (con.user.getLista().size() == 0) {
            present.add(0, "No hay presentaciones para " + user.getNombreusuario());
        } else {
            present = con.user.getLista();
        }
        Log.i("HomeFragment_list", present.toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(root.getContext().getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, present);
        presList.setAdapter(adapter);*/

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        /*presList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                /*String fila = con.user.getLista().get(position);
                String[] parts = fila.split("-");
                String id = parts[0];

            }
        });*/

        enviaSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sesion = session.getText().toString().trim();
                String seleccionado = presList.getSelectedItem().toString();
                con.crearSesion(sesion,seleccionado,root);
                Toast.makeText(root.getContext().getApplicationContext(), "Sesion: "+sesion+" - "+seleccionado, Toast.LENGTH_LONG).show();
                //Toast.makeText(root.getContext().getApplicationContext(), "Rellena todos los campos", "Rellena todos los campos".length()).show();
            }
        });



        return root;
    }
}