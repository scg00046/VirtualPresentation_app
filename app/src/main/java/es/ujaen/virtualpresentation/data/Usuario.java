package es.ujaen.virtualpresentation.data;

public class Usuario {
    private String nombreusuario;
    private String nombre;
    private String apellidos;

    public Usuario(String nombreusuario, String nombre, String apellidos) {
        this.nombreusuario = nombreusuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
    }

    public Usuario(String user){
        String[] atributo = user.split(",");
        this.nombreusuario = atributo[0];
        this.nombre = atributo[1];
        this.apellidos = atributo[2];
    }

    public Usuario() {
    }

    public String getNombreusuario() {
        return nombreusuario;
    }

    public void setNombreusuario(String nombreusuario) {
        this.nombreusuario = nombreusuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
}
