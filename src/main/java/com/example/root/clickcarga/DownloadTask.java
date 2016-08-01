package com.example.root.clickcarga;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;


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


    public List<Midia> lista;

    public DownloadTask(List<Midia> lista) {

        this.lista = lista;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        System.out.println("---------------thread download --"+lista);
        for(Midia m:lista) {
            File f = new File("sdcard/Pictures/"+m.getArquivo());
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
        System.out.println("---------------------- acabou download");

        apagaArquivosNaoUsados();
        return null;
    }



    public void apagaArquivosNaoUsados(){
        File f = new File("/sdcard/Pictures/");
        File[] filelist = f.listFiles();
        for(int i=0;i<filelist.length;i++){
            Boolean apagar = false;
            for(Midia m : lista) {
                if(m.getArquivo()==filelist[i].getName()){
                    apagar = true;
                }
            }
            if(apagar){
                filelist[i].delete();
            }
        }
    }
    
}