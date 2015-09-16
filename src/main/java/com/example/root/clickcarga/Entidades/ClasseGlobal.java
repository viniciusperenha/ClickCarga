package com.example.root.clickcarga.Entidades;

import android.app.Application;

import java.util.List;

/**
 * Created by root on 15/09/15.
 */
public class ClasseGlobal extends Application {
    public List<Midia> programacaovigente;

    public List<Midia> getProgramacaovigente() {
        return programacaovigente;
    }

    public void setProgramacaovigente(List<Midia> programacaovigente) {
        this.programacaovigente = programacaovigente;
    }

    public String[] getClasses() {
        return new String[]{"Midia"};
    }
}
