package es.ujaen.virtualpresentation.data;

/**
 * Clase Presentations, parámetros de las presentaciones almacenadas en el servidor
 * @author Sergio Caballero Garrido
 */
public class Presentations {
    private int idpresentacion;
    private String presentacion;
    private int paginas;

    public Presentations() {
    }

    public Presentations(int idpresentacion, String presentacion, int paginas) {
        this.idpresentacion = idpresentacion;
        this.presentacion = presentacion;
        this.paginas = paginas;
    }

    public int getIdpresentacion() {
        return idpresentacion;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public int getPaginas() {
        return paginas;
    }

}
