package com.example.root.clickcarga;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.root.clickcarga.Entidades.Midia;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;



public class MainActivity extends ActionBarActivity{

    public VideoView video;
    public ImageView imagem;

    public Bitmap myBitmap = null;
    public int duracao = 0;
    public static SQLiteDatabase db;
    public String servidorrest = "http://192.168.1.44:8084/clickCarga/rest/rest/";
    public String servidorarquivo = "http://192.168.1.44:8084/clickCarga/arquivoPasta/?id=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        try {
            Runtime.getRuntime().exec("service call activity 42 s16 com.android.systemui");
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);

        
        video = (VideoView)  findViewById(R.id.video);
        imagem = (ImageView) findViewById(R.id.imagem);
        video.setVisibility(View.INVISIBLE);
        imagem.setVisibility(View.INVISIBLE);

        criaBanco();
        if(!verificaTabelasBanco()){
            criaTabelasBanco();
        }
        File dir = new File("sdcard/Pictures");
        if (!dir.exists()){
            dir.mkdirs();
        }

        if(checkConexaoInternet(getApplicationContext())) {
            consultaWebService();
        }

    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(buscaProgramacao()!=null) {
            new trocadorDeImagens().execute();
        }
    }

    public void StartSistema(View v){
        if(buscaProgramacao()!=null) {
            new trocadorDeImagens().execute();
        }
    }

    private void criaBanco() {
        try {
            db = openOrCreateDatabase("bridge", Activity.MODE_PRIVATE, null);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public boolean verificaTabelasBanco(){
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name= 'midia'",null);
        return c.moveToFirst();
    }

    public void consultaWebService(){
        String imei = buscaImeiAparelho();
        ServidorRestTask servidorRestTask = new ServidorRestTask(servidorrest, servidorarquivo, imei, db);
        servidorRestTask.execute();

    }

    public String buscaImeiAparelho(){
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public void abrirFoto(File f){
        Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
        video.setVisibility(View.INVISIBLE);
        imagem.setVisibility(View.VISIBLE);
        imagem.setImageBitmap(myBitmap);
    }

    public void abrirVideo(File f){
        video.setVisibility(View.VISIBLE);
        imagem.setVisibility(View.INVISIBLE);
        video.setVideoPath(f.getAbsolutePath());
        video.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean checkConexaoInternet(Context c)
    {
        ConnectivityManager connectivity = (ConnectivityManager)getApplicationContext().getSystemService(c.CONNECTIVITY_SERVICE);

        if (connectivity != null)
        {
            NetworkInfo[] inf = connectivity.getAllNetworkInfo();
            if (inf != null)
                for (int i = 0; i < inf.length; i++)
                    if (inf[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

    public void criaTabelasBanco()  {
        try {

            for (String s : getClasses()) {
                Class<?> aClass = Class.forName("com.example.root.clickcarga.Entidades." + s);
                StringBuilder sb = new StringBuilder();
                sb.append("CREATE TABLE if not exists " + s.toLowerCase() + "(");
                for (Field f : aClass.getDeclaredFields()) {
                    Class<?> type = f.getType();
                    sb.append(f.getName() + " " + tipoSql(type));
                    if (f.getName().equals("id")) {
                        sb.append(" NOT NULL PRIMARY KEY");
                    }
                    sb.append(",");
                }
                sb.setLength(sb.length() - 1);
                sb.append(")");

                db.execSQL(sb.toString());
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public String[] getClasses() {
        return new String[]{"Midia"};
    }

    public String tipoSql(Class tipo){

        if (tipo.equals(String.class)) return "varchar(100)";
        if (tipo.equals(Character.class)) return "char(1)";
        if (tipo.equals(Date.class)) return "date";
        if (tipo.equals(Double.class)) return "double(7,2)";
        if (tipo.equals(double.class)) return "double(7,2)";
        if (tipo.equals(BigInteger.class)) return "bigint";
        if (tipo.equals(Integer.class)) return "int";
        return "long";
    }

    public List<Midia> buscaProgramacao(){
        Cursor c = db.rawQuery("select * from midia order by ordem",null);
        if(c.moveToFirst()){
            List<Midia> mds = new LinkedList<Midia>();
            for(int i=0;i<c.getCount();i++){
                Midia m = new Midia();
                m.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                m.setArquivo(c.getString(c.getColumnIndexOrThrow("arquivo")));
                m.setDuracao(c.getLong(c.getColumnIndexOrThrow("duracao")));
                m.setOrdem(c.getLong(c.getColumnIndexOrThrow("ordem")));
                mds.add(m);
                c.moveToNext();
            }
            return mds;
        }
        return null;
    }

    class trocadorDeImagens extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {

            List<Midia> m = buscaProgramacao();
            System.out.println("------------------"+m);
            for(int i = 0; i< m.size();i++){
                System.out.println("/sdcard/Pictures/"+m.get(i).getArquivo());
                File file = new File("/sdcard/Pictures/"+m.get(i).getArquivo());
                if(file.exists()) {
                    if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".png")) {
                        publishProgress(file.getAbsolutePath());
                        try {
                            Thread.sleep(m.get(i).getDuracao() * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (file.getName().toLowerCase().endsWith(".3gp") || file.getName().toLowerCase().endsWith(".mp4")) {
                        publishProgress(file.getAbsolutePath());

                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(duracao);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //if(i==m.size()-1){
                //   if(checkConexaoInternet(getApplicationContext())){
                //        consultaWebService();
                //    }
                //    m = buscaProgramacao();
                //    i=-1;
                //}

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            consultaWebService();
            new trocadorDeImagens().execute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            File file=new File(values[0]);
            if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".gif") || file.getName().toLowerCase().endsWith(".jpeg")) {
                if(myBitmap!=null) {
                    myBitmap=null;
                }
                BitmapFactory.Options opts=new BitmapFactory.Options();
                opts.inDither=false;
                opts.inTempStorage=new byte[1024 * 1024];
                myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),opts);
                video.setVisibility(View.INVISIBLE);
                imagem.setVisibility(View.VISIBLE);

                //imagem.setImageBitmap(myBitmap);
                imagem.post(new Runnable() {
                    @Override
                    public void run() {
                        imagem.setImageBitmap(myBitmap);
                    }
                });

            } else if (file.getName().toLowerCase().endsWith(".avi") || file.getName().toLowerCase().endsWith(".mp4")|| file.getName().toLowerCase().endsWith(".mpeg")) {
                video.setVisibility(View.VISIBLE);
                imagem.setVisibility(View.INVISIBLE);
                video.setVideoPath(file.getAbsolutePath());
                video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        video.start();
                        duracao = video.getDuration();

                    }
                });
            }
        }


    }


}




