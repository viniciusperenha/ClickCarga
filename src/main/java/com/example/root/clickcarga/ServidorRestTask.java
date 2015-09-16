package com.example.root.clickcarga;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.example.root.clickcarga.Entidades.ClasseGlobal;
import com.example.root.clickcarga.Entidades.Midia;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vinicius on 11/09/15.
 */
public class ServidorRestTask extends AsyncTask<String, String, String> {

    private String servidor;
    private String imei;
    private static SQLiteDatabase db;
    public Context contexto;
    private String servidorarquivo;
    private List<Midia> listamidias;

    public ServidorRestTask(Context contexto, String servidor, String servidorarquivo,String imei, SQLiteDatabase db) {
        this.servidor = servidor;
        this.imei = imei;
        this.contexto = contexto;
        this.servidorarquivo = servidorarquivo;
        this.db = db;

    }

    @Override
    protected String doInBackground(String... params) {
        excluiRegistros();
        JSONArray lista = consultaGenerica();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Midia>>() {
        }.getType();
        listamidias = gson.fromJson(lista.toString(), listType);
        inserir(listamidias);

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        ClasseGlobal classeGlobal = (ClasseGlobal) contexto.getApplicationContext();
        if(listamidias.equals(classeGlobal.getProgramacaovigente())) {
            DownloadTask downloadTask = new DownloadTask(contexto, listamidias);
            downloadTask.execute(servidorarquivo);
        }

    }

    public JSONArray consultaGenerica() {

        try {
            DefaultHttpClient dhc = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(servidor + imei);
            HttpResponse resposta = null;
            resposta = dhc.execute(httpGet);
            String res = EntityUtils.toString(resposta.getEntity());
            JSONArray jsa = new JSONArray(res);
            return jsa;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }



    public void excluiRegistros(){
        db.execSQL("delete from midia");
    }

    public void inserir(List l) {
        if (l.isEmpty()) {
            return;
        }
        try {

            for (Object obj : l) {

                Class classe = obj.getClass();
                ContentValues cv = new ContentValues();
                for (Field f : classe.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object valor = f.get(obj);
                    if (valor != null) {
                        cv.put(f.getName(), valor.toString());
                    }
                }
                db.insert(classe.getSimpleName().toLowerCase(), null, cv);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


}
