package com.example.energymonitor;

public class UserInformation {

    public String name;
    public String datanascimento;
    public String telefone;
    public String email;

    public  UserInformation() {

    }

    public UserInformation(String name, String datanascimento, String telefone, String email){
        this.name = name;
        this.datanascimento = datanascimento;
        this.telefone = telefone;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatanascimento() {
        return datanascimento;
    }

    public void setDatanascimento(String datanascimento) {
        this.datanascimento = datanascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
