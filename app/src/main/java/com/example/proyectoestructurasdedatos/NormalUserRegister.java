package com.example.proyectoestructurasdedatos;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NormalUserRegister extends AppCompatActivity {

    EditText ET_Nombre, ET_Apellido, ET_Nacimiento, ET_Documento;
    RadioButton RB_Discapacidad;
    Button BT_Finalizar;
    private FirebaseAuth mAuth;
    FirebaseUser user;
    String UID;

    public final Calendar c = Calendar.getInstance();
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);
    int aStamp, mStamp, dStamp;

    private final String HOST_IP = "192.168.1.13";
    private final String CARPETA_SCRIPTS = "archivos_conexion_bd";
    private final String PUERTO = "80";
    private final String NOMBRE_SCRIPT = "registrarUsuario";
    private String URL;

    private String correo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_user_register);

        this.correo = this.getIntent().getExtras().getString("email");

        mAuth = FirebaseAuth.getInstance();

        ET_Nombre = (EditText) findViewById(R.id.NombreEditText);
        ET_Apellido = (EditText) findViewById(R.id.ApellidoEditText);
        ET_Nacimiento = (EditText) findViewById(R.id.NacimientoEditText);
        ET_Documento = (EditText) findViewById(R.id.DocumentoEditText);
        RB_Discapacidad = (RadioButton) findViewById(R.id.DiscaSiRadio);
        BT_Finalizar = (Button) findViewById(R.id.BotonFinalizar);

        ET_Nacimiento.setText(anio + "/" + (mes + 1) + "/" + dia);

        BT_Finalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalizarRegistro();
            }
        });

        ET_Nacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                obtenerFecha();
                closeKeyboard();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        user = mAuth.getCurrentUser();
        UID = user.getUid();
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void obtenerFecha(){
        DatePickerDialog recogerFecha = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final int mesActual = month + 1;
                String diaFormateado = (dayOfMonth < 10) ? "0" + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                String mesFormateado = (mesActual < 10) ? "0" + String.valueOf(mesActual):String.valueOf(mesActual);

                aStamp = year;
                mStamp = month;
                dStamp = dayOfMonth;
                ET_Nacimiento.setText(year + "/" + mesFormateado + "/" + diaFormateado);
            }
        },anio, mes, dia);
        recogerFecha.getDatePicker().setMaxDate(System.currentTimeMillis());
        recogerFecha.show();
    }

    private void finalizarRegistro(){
        String nombre = ET_Nombre.getText().toString().trim();
        String apellido = ET_Apellido.getText().toString().trim();
        String nacimiento = ET_Nacimiento.getText().toString().trim();
        String documento = ET_Documento.getText().toString().trim();
        Boolean discapacitado = RB_Discapacidad.isChecked();
        String datePattern = "[0-9][0-9][0-9][0-9]/[0-9][0-9]/[0-9][0-9]";

        if(nombre.isEmpty()){
            ET_Nombre.setError("Ingrese un nombre");
            return;
        }

        if(apellido.isEmpty()){
            ET_Apellido.setError("Ingrese un apellido");
            return;
        }

        if(nacimiento.isEmpty()){
            ET_Apellido.setError("Ingrese una Fecha de Nacimiento");
            return;
        } else if(!nacimiento.matches(datePattern)){
            ET_Nacimiento.setError("Ingrese una Fecha de Nacimiento válida");
            return;
        }

        if(documento.isEmpty()){
            ET_Apellido.setError("Ingrese un Número de Identificación");
            return;
        }

        //Codigo para subir los datos a la base de datos
        //UID es la Primary Key del Usuario (Dada por Authentication)
        this.URL = "http://" + HOST_IP + ":" + PUERTO + "/" + CARPETA_SCRIPTS + "/" + NOMBRE_SCRIPT + ".php";
        registrarUsuario();

        Toast.makeText(this, "Funcionó", Toast.LENGTH_SHORT).show();
    }

    private void registrarUsuario() {
        StringRequest solicitud = new StringRequest(Request.Method.POST, this.URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "OPERACION EXITOSA", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("id_usuario", UID);
                parametros.put("id_documento", ET_Documento.getText().toString(). trim());
                parametros.put("nombre", ET_Nombre.getText().toString().trim());
                parametros.put("apellido", ET_Apellido.getText().toString().trim());
                parametros.put("fecha_nacimiento", ET_Nacimiento.getText().toString(). trim());
                parametros.put("correo", correo);

                if (RB_Discapacidad.isChecked()) {
                    parametros.put("discapacitado", "True");
                } else {
                    parametros.put("discapacitado", "False");
                }

                return parametros;
            }
        };

        RequestQueue colaSolicitud = Volley.newRequestQueue(this);
        colaSolicitud.add(solicitud);
    }
}