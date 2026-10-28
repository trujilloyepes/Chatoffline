package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Aquí está la lógica del chat. Para guardar y leer usa GestorXML.
public class GestorChat {
    private final GestorXML gestorXML = new GestorXML();
    // Registra un usuario nuevo y lo guarda en usuarios.xml.
    public void registrarUsuario(String nombre) throws JAXBException {
        //Comprobar que el nombre no está vacío
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        nombre = nombre.trim();
        //Si ya existe, error
        if (buscarNombreRegistrado(nombre) != null) {
            throw new IllegalArgumentException("El usuario '" + nombre + "' ya existe.");
        }
        //Añadirlo y guardar
        ListaUsuarios lista = gestorXML.leerUsuarios();
        lista.getUsuarios().add(new Usuario(nombre));
        gestorXML.guardarUsuarios(lista);
    }
    // Devuelve true si el usuario está registrado.
    public boolean existeUsuario(String nombre) throws JAXBException {
        return buscarNombreRegistrado(nombre) != null;
    }
    // Devuelve los nombres de todos los usuarios registrados.
    public List<String> listarUsuarios() throws JAXBException {
        List<String> nombres = new ArrayList<>();
        ListaUsuarios lista = gestorXML.leerUsuarios();
        for (Usuario u : lista.getUsuarios()) {
            nombres.add(u.getNombre());
        }
        return nombres;
    }
    // Busca un usuario sin distinguir mayúsculas y devuelve su nombre TAL Y COMO
    // se registró. Si no existe, devuelve null.
    private String buscarNombreRegistrado(String nombre) throws JAXBException {
        if (nombre == null) {
            return null;
        }
        ListaUsuarios lista = gestorXML.leerUsuarios();
        for (Usuario u : lista.getUsuarios()) {
            if (u.getNombre().equalsIgnoreCase(nombre.trim())) {
                return u.getNombre();
            }
        }
        return null;
    }
    // Crea un mensaje y lo guarda en mensajes.xml.
    // Los mensajes solo se AÑADEN: no existe ningún método para borrarlos.
    public void enviarMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
        //Comprobar que el emisor y el destinatario están registrados
        String nombreEmisor = buscarNombreRegistrado(emisor);
        String nombreDestinatario = buscarNombreRegistrado(destinatario);
        if (nombreEmisor == null) {
            throw new IllegalArgumentException("El emisor '" + emisor + "' no está registrado.");
        }
        if (nombreDestinatario == null) {
            throw new IllegalArgumentException("El destinatario '" + destinatario + "' no está registrado.");
        }
        //No se puede enviar un mensaje a uno mismo
        if (nombreEmisor.equals(nombreDestinatario)) {
            throw new IllegalArgumentException("No puedes enviarte un mensaje a ti mismo.");
        }
        //El contenido no puede estar vacío
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }
        //Leer los mensajes que ya existen (así no se sobreescribe nada)
        ListaMensajes lista = gestorXML.leerMensajes();
        //Calcular el id nuevo: el id más alto que haya + 1
        int idMaximo = 0;
        for (Mensaje m : lista.getMensajes()) {
            if (m.getId() > idMaximo) {
                idMaximo = m.getId();
            }
        }
        //Crear la fecha y hora actual en formato ISO (2026-10-28T13:45:30)
        String fecha = LocalDateTime.now().withNano(0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        //Crear el mensaje, ponerle su id, añadirlo a la lista y guardar
        Mensaje mensaje = new Mensaje(nombreEmisor, nombreDestinatario, contenido.trim(), fecha);
        mensaje.setId(idMaximo + 1);
        lista.getMensajes().add(mensaje);
        gestorXML.guardarMensajes(lista);
    }
    // Devuelve todos los mensajes entre dos usuarios, en los DOS sentidos:
    // los que A envió a B y los que B envió a A.
    // Salen en el orden en que se guardaron, que es el orden cronológico,
    // porque los mensajes solo se añaden al final del XML.
    public List<Mensaje> consultarConversacion(String usuarioA, String usuarioB) throws JAXBException {
        //Comprobar que los dos usuarios existen
        String a = buscarNombreRegistrado(usuarioA);
        String b = buscarNombreRegistrado(usuarioB);

        if (a == null) {
            throw new IllegalArgumentException("El usuario '" + usuarioA + "' no está registrado.");
        }
        if (b == null) {
            throw new IllegalArgumentException("El usuario '" + usuarioB + "' no está registrado.");
        }
        if (a.equals(b)) {
            throw new IllegalArgumentException("Tienes que elegir dos usuarios distintos.");
        }
        //Leer todos los mensajes y quedarnos solo con los de estos dos usuarios
        ListaMensajes lista = gestorXML.leerMensajes();
        List<Mensaje> resultado = new ArrayList<>();

        for (Mensaje m : lista.getMensajes()) {
            boolean deAaB = m.getEmisor().equals(a) && m.getDestinatario().equals(b);
            boolean deBaA = m.getEmisor().equals(b) && m.getDestinatario().equals(a);

            if (deAaB || deBaA) {
                resultado.add(m);
            }
        }
        return resultado;
    }
    // No distingue mayúsculas: buscar "hola" también encuentra "Hola".
    public List<Mensaje> filtrarPorPalabra(List<Mensaje> mensajes, String palabra) {
        if (palabra == null || palabra.trim().isEmpty()) {
            throw new IllegalArgumentException("La palabra a buscar no puede estar vacía.");
        }
        String buscada = palabra.trim().toLowerCase();

        List<Mensaje> resultado = new ArrayList<>();
        for (Mensaje m : mensajes) {
            if (m.getContenido().toLowerCase().contains(buscada)) {
                resultado.add(m);
            }
        }
        return resultado;
    }
    // La fecha se escribe como año-mes-día, por ejemplo 2026-10-28.
    public List<Mensaje> filtrarPorFecha(List<Mensaje> mensajes, String fechaTexto) {
        LocalDate dia;
        try {
            // Si el texto no tiene el formato correcto, parse lanza un error
            dia = LocalDate.parse(fechaTexto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha no válida. Usa el formato año-mes-día, por ejemplo 2026-10-28.");
        }
        List<Mensaje> resultado = new ArrayList<>();
        for (Mensaje m : mensajes) {
            // La fecha del mensaje empieza por el día: "2026-10-28T13:45:30"
            if (m.getFecha().startsWith(dia.toString())) {
                resultado.add(m);
            }
        }
        return resultado;
    }
    // Convierte un mensaje en una línea de texto para mostrarlo por pantalla.
    public String formatearMensaje(Mensaje m) {
        String fecha = m.getFecha().replace("T", " ");
        String texto = "[" + fecha + "] " + m.getEmisor() + ": " + m.getContenido();

        // Si el mensaje tiene adjunto, añadimos una línea más
        if (m.getAdjunto() != null) {
            texto = texto + "\n      Adjunto: " + m.getAdjunto().getNombre()
                    + " (" + m.getAdjunto().getTipo() + ", " + m.getAdjunto().getTamano() + " bytes)";
        }
        return texto;
    }
    public Map<String, Long> estadisticas(String usuarioA, String usuarioB) throws JAXBException {
        throw new UnsupportedOperationException("Pendiente: estadisticas");
    }

    public void exportarConversacion(String usuarioA, String usuarioB, Path destino)
            throws JAXBException, IOException {
        throw new UnsupportedOperationException("Pendiente: exportarConversacion");
    }

    public void adjuntarArchivo(Mensaje mensaje, Path archivoOrigen) throws IOException, JAXBException {
        throw new UnsupportedOperationException("Pendiente: adjuntarArchivo");
    }
}