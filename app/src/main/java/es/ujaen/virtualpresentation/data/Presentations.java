package es.ujaen.virtualpresentation.data;

public class Presentations {
    private int idpresentacion;
    private String presentacion;
    private int paginas;
    //private String nombreUsuario;

    public Presentations() {
    }

    public Presentations(int idpresentacion, String presentacion, int paginas/*, String nombreUsuario*/) {
        this.idpresentacion = idpresentacion;
        this.presentacion = presentacion;
        this.paginas = paginas;
        //this.nombreUsuario = nombreUsuario;
    }

    public int getIdpresentacion() {
        return idpresentacion;
    }

    public void setIdpresentacion(int idpresentacion) {
        this.idpresentacion = idpresentacion;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public int getPaginas() {
        return paginas;
    }

    public void setPaginas(int paginas) {
        this.paginas = paginas;
    }

    /*public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }*/
}
