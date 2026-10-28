package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "usuarios")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListaUsuarios {

    // Aquí se guardan los usuarios. En el XML cada uno será una etiqueta <usuario>.
    @XmlElement(name = "usuario")
    private List<Usuario> usuarios = new ArrayList<>();

    // Constructor vacío: JAXB lo necesita para funcionar.
    public ListaUsuarios() { }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
