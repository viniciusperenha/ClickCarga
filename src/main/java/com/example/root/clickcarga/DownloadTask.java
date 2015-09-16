package com.example.root.clickcarga;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.Toast;

import com.example.root.clickcarga.Entidades.ClasseGlobal;
import com.example.root.clickcarga.Entidades.Midia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by vinicius on 04/09/15.
 */
public class DownloadTask extends AsyncTask<String, Integer, String> {

    private Context context;
    public List<Midia> lista;

    public DownloadTask(Context context, List<Midia> lista) {
        this.context = context;
        this.lista = lista;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        for(Midia m:lista) {
            File f = new File("/sdcard/Pictures/" + m.getArquivo());
            if (!f.exists()) {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(sUrl[0]+m.getArquivo());
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    //testa conexao
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        return "Servidor nao encontrado - HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage();
                    }

                    // download the file
                    input = connection.getInputStream();
                    output = new FileOutputStream("/sdcard/Pictures/"+m.getArquivo());
                    byte data[] = new byte[4096];

                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        output.write(data, 0, count);
                    }
                } catch (Exception e) {
                    return e.toString();
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    } catch (IOException ignored) {
                    }

                    if (connection != null)
                        connection.disconnect();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {

        if (result != null)
            Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();

        final ClasseGlobal classeGlobal = (ClasseGlobal) context.getApplicationContext();
        classeGlobal.setProgramacaovigente(lista);
    }
}