package com.example.proyectoestructurasdedatos;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyectoestructurasdedatos.utilidades.DatosConexion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdminUserQuery extends AppCompatActivity
        implements Response.Listener<JSONArray>, Response.ErrorListener, DatosConexion {

    Button crearCola, actualizarCola, desencolarBTN;
    ListView listaCola;

    String[][] listadoOrdenado;

    RequestQueue colaSolicitud;
    JsonArrayRequest jsonArrayRequest;

    private ColaDePrioridad colaDePrioridad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_query);

        colaDePrioridad = new ColaDePrioridad();

        colaSolicitud = Volley.newRequestQueue(this);

        crearCola = (Button) findViewById(R.id.botonCrearCola);
        actualizarCola = (Button) findViewById(R.id.BotonActualizarLista);
        desencolarBTN = (Button) findViewById(R.id.BTNDesencolar);
        listaCola = (ListView) findViewById(R.id.listaColas);

        listaCola.setAdapter(new listAdapter(this,2));

        cargarWebService();

        crearCola.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearCola();
                crearCola.setEnabled(false);
            }
        });

        actualizarCola.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (colaDePrioridad.getLongitud() > 1) {
                    actualizarCola();
                }
            }
        });

        desencolarBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!colaDePrioridad.estaVacia()) {
                    desencolarCola();
                }
            }
        });
    }

    public void cargarWebService () {
        jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_USUARIOS_COLA, null, this, this);
        colaSolicitud.add(jsonArrayRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(getApplicationContext(), "ERROR DE CONEXIÓN", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONArray response) {
        try {
            retornarYRegistrarUsuariosCola(response);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void retornarYRegistrarUsuariosCola (JSONArray response) throws JSONException{
        JSONObject jsonObject = null;
        for (int i = 0; i < response.length(); i++) {
            jsonObject = response.getJSONObject(i);
            encolarUsuario(jsonObject);
        }
    }

    private void encolarUsuario (JSONObject jsonObject) throws JSONException{
        Usuario usuario = new Usuario();
        usuario.setNombre(jsonObject.getString("nombre"));
        usuario.setDocumento(jsonObject.getInt("id_documento"));
        usuario.setDistancia(
                calcularDistancia(
                        jsonObject.getDouble("latitud"),
                        jsonObject.getDouble("longitud")
                )
        );
        colaDePrioridad.encolar(usuario);
    }

    private double calcularDistancia(double latitud, double longitud) {
        return Math.sqrt(Math.pow(latitud, 2) + Math.pow(longitud, 2));
    }

    private void crearCola(){
        listadoOrdenado = new String[colaDePrioridad.getLongitud()][2];

        //Interfaz de cargando lista
        listaCola.setAdapter(new listAdapter(this,0));

        //Interfaz en caso que ocurra un error
        // listaCola.setAdapter(new listAdapter(this,1));

        listadoOrdenado = colaDePrioridad.devolverInformacionUsuarios();

        if(listadoOrdenado == null){
            listaCola.setAdapter(new listAdapter(this, 2));
        }
        else{
            listaCola.setAdapter(new listAdapter(this, listadoOrdenado));
        }

    }

    private void actualizarCola(){
        listadoOrdenado = new String[colaDePrioridad.getLongitud()][2];

        //Interfaz de cargando lista
        listaCola.setAdapter(new listAdapter(this,0));

        //Interfaz en caso que ocurra un error
        // listaCola.setAdapter(new listAdapter(this,1));

        if (!colaDePrioridad.estaVacia()) {
            colaDePrioridad.alterarDistancia();
            colaDePrioridad.organizar();
            listadoOrdenado = colaDePrioridad.devolverInformacionUsuarios();
        }

        if(listadoOrdenado == null){
            listaCola.setAdapter(new listAdapter(this, 2));
        }
        else{
            listaCola.setAdapter(new listAdapter(this, listadoOrdenado));
        }
    }

    private void desencolarCola(){
        colaDePrioridad.desencolar();
        actualizarCola();
    }
}