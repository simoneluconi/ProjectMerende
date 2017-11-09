package com.simoneluconi.projectmerende.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

@Table(name = "MERENDA")
public class Merenda {

    @SerializedName("id")
    @Expose
    @Unique
    private long id;
    @SerializedName("Nome")
    @Expose
    private String nome;
    @SerializedName("Prezzo")
    @Expose
    private double prezzo;
    @SerializedName("Prezzo_Doppio")
    @Expose
    private double prezzoDoppio;
    @SerializedName("qmax")
    @Expose
    private int qmax;

    private int currentQty;

    public String getNome() {
        return nome;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public double getPrezzoDoppio() {
        return prezzoDoppio;
    }

    public int getQmax() {
        return qmax;
    }

    public int getCurrentQty() {
        return currentQty;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCurrentQty(int quantity) {
        this.currentQty = quantity;
    }

    public Merenda() {

    }
}