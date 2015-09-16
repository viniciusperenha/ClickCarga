package com.example.root.clickcarga.Entidades;

/**
 * Created by Vinicius on 15/09/15.
 */
public class Midia {
    private Long id;
    private String arquivo;
    private Long duracao;
    private Long ordem;

    public Long getOrdem() {
        return ordem;
    }

    public void setOrdem(Long ordem) {
        this.ordem = ordem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArquivo() {
        return arquivo;
    }

    public void setArquivo(String arquivo) {
        this.arquivo = arquivo;
    }

    public Long getDuracao() {
        return duracao;
    }

    public void setDuracao(Long duracao) {
        this.duracao = duracao;
    }
}
