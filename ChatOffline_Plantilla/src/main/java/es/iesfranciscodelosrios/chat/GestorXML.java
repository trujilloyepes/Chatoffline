package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
public class GestorXML {

    // Archivos donde se guardan los datos. Se crean solos al ejecutar.
    private File archivoUsuarios = new File("usuarios.xml");
    private File archivoMensajes = new File("mensajes.xml");

    // Guarda la lista de usuarios en usuarios.xml
    public void guardarUsuarios(ListaUsuarios usuarios) throws JAXBException {
        //Preparamos JAXB para trabajar con la clase ListaUsuarios
        JAXBContext contexto = JAXBContext.newInstance(ListaUsuarios.class);
        //Creamos el "escritor" de XML
        Marshaller escritor = contexto.createMarshaller();
        //Esto hace que el XML se vea ordenado, con cada etiqueta en su línea
        escritor.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //Escribimos el objeto en el archivo
        escritor.marshal(usuarios, archivoUsuarios);
    }

    // Lee usuarios.xml y devuelve la lista de usuarios
    public ListaUsuarios leerUsuarios() throws JAXBException {
        // Si el archivo todavía no existe, devolvemos una lista vacía
        if (!archivoUsuarios.exists()) {
            return new ListaUsuarios();
        }
        //Preparamos JAXB
        JAXBContext contexto = JAXBContext.newInstance(ListaUsuarios.class);
        //Creamos el "lector" de XML
        Unmarshaller lector = contexto.createUnmarshaller();
        //Leemos el archivo. Hay que decirle que el resultado es una ListaUsuarios
        ListaUsuarios resultado = (ListaUsuarios) lector.unmarshal(archivoUsuarios);
        return resultado;
    }
    // Guarda la lista de mensajes en mensajes.xml
    public void guardarMensajes(ListaMensajes mensajes) throws JAXBException {
        JAXBContext contexto = JAXBContext.newInstance(ListaMensajes.class);
        Marshaller escritor = contexto.createMarshaller();
        escritor.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        escritor.marshal(mensajes, archivoMensajes);
    }

    // Lee mensajes.xml y devuelve la lista de mensajes
    public ListaMensajes leerMensajes() throws JAXBException {
        if (!archivoMensajes.exists()) {
            return new ListaMensajes();
        }
        JAXBContext contexto = JAXBContext.newInstance(ListaMensajes.class);
        Unmarshaller lector = contexto.createUnmarshaller();
        ListaMensajes resultado = (ListaMensajes) lector.unmarshal(archivoMensajes);
        return resultado;
    }
}
