package com.example.energymonitor;

public class GastosInformation {
    
    public Double preco;
    public Double icms;
    public Double pis;
    public Double cofins;
    
    public  GastosInformation() {
        
    }

    public GastosInformation(Double preco, Double icms, Double pis, Double cofins) {
        this.preco = preco;
        this.icms = icms;
        this.pis = pis;
        this.cofins = cofins;
    }

    public Double getpreco() {
        return preco;
    }

    public void setpreco(Double preco) {
        this.preco = preco;
    }

    public Double getIcms() {
        return icms;
    }

    public void setIcms(Double icms) {
        this.icms = icms;
    }

    public Double getPis() {
        return pis;
    }

    public void setPis(Double pis) {
        this.pis = pis;
    }

    public Double getCofins() {
        return cofins;
    }

    public void setCofins(Double cofins) {
        this.cofins = cofins;
    }
}
