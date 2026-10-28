package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Mensaje {

    @XmlAttribute
    private int id;

    private String emisor;
    private String destinatario;
    private String contenido;
    private String fecha;     // formato ISO-8601, por ejemplo: 2026-10-28T13:45:30
    private Adjunto adjunto;  // null si el mensaje no tiene adjunto

    // Constructor vacío.
    public Mensaje() { }

    public Mensaje(String emisor, String destinatario, String contenido, String fecha) {
        this.emisor = emisor;
        this.destinatario = destinatario;
        this.contenido = contenido;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmisor() { return emisor; }
    public void setEmisor(String emisor) { this.emisor = emisor; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public Adjunto getAdjunto() { return adjunto; }
    public void setAdjunto(Adjunto adjunto) { this.adjunto = adjunto; }
}