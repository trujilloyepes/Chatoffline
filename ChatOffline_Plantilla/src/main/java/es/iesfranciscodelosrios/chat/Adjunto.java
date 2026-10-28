package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Adjunto {
    private String nombre; // nombre del archivo, por ejemplo "foto.png"
    private String ruta;   // dónde está guardado, por ejemplo "media/foto.png"
    private long tamano;   // tamaño en bytes
    private String tipo;   // tipo de archivo, por ejemplo "png" o "pdf"

    // Constructor vacío.
    public Adjunto() { }

    public Adjunto(String nombre, String ruta, long tamano, String tipo) {
        this.nombre = nombre;
        this.ruta = ruta;
        this.tamano = tamano;
        this.tipo = tipo;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }
    public long getTamano() { return tamano; }
    public void setTamano(long tamano) { this.tamano = tamano; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
