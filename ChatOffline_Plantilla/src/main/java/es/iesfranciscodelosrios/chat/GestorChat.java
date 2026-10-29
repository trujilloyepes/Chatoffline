package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GestorChat {
    private final GestorXML gestorXML = new GestorXML();
    private final AnalizadorConversacion analizador = new AnalizadorConversacion();
    private final GestorAdjuntos gestorAdjuntos = new GestorAdjuntos();
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
    // Envía un mensaje normal (sin adjunto).
    public void enviarMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
        Mensaje mensaje = crearMensaje(emisor, destinatario, contenido);
        guardarMensaje(mensaje);
    }
    // Valida los datos y crea el mensaje (todavía sin guardar y sin id).
    private Mensaje crearMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
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
        //Crear la fecha y hora actual en formato ISO (2026-10-28T13:45:30)
        String fecha = LocalDateTime.now().withNano(0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new Mensaje(nombreEmisor, nombreDestinatario, contenido.trim(), fecha);
    }
    // Le pone su id al mensaje y lo añade a mensajes.xml.
    private void guardarMensaje(Mensaje mensaje) throws JAXBException {
        //Leer los mensajes que ya existen (así no se sobreescribe nada)
        ListaMensajes lista = gestorXML.leerMensajes();
        //Calcular el id nuevo: el id más alto que haya + 1
        int idMaximo = 0;
        for (Mensaje m : lista.getMensajes()) {
            if (m.getId() > idMaximo) {
                idMaximo = m.getId();
            }
        }
        mensaje.setId(idMaximo + 1);
        //Añadirlo al final de la lista y guardar
        lista.getMensajes().add(mensaje);
        gestorXML.guardarMensajes(lista);
    }
    // Devuelve todos los mensajes entre dos usuarios, en los DOS sentidos:
    // los que A envió a B y los que B envió a A.
    public List<Mensaje> consultarConversacion(String usuarioA, String usuarioB) throws JAXBException {
        // 1. Comprobar que los dos usuarios existen
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
        // 2. Leer todos los mensajes y quedarnos solo con los de estos dos usuarios
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
    // Convierte un mensaje en texto para mostrarlo por pantalla.
    // Ejemplo: #3 [2026-10-28 13:45:30] ana: Hola Luis
    // Si tiene adjunto, añade una línea más. Si el archivo ya no existe, lo marca.
    public String formatearMensaje(Mensaje m) {
        String fecha = m.getFecha().replace("T", " ");
        String texto = "#" + m.getId() + " [" + fecha + "] " + m.getEmisor() + ": " + m.getContenido();
        Adjunto adjunto = m.getAdjunto();
        if (adjunto != null) {
            if (gestorAdjuntos.existe(adjunto)) {
                texto = texto + "\n      Adjunto: " + adjunto.getNombre()
                        + " (" + adjunto.getTipo() + ", "
                        + String.format("%.1f KB", adjunto.getTamano() / 1024.0) + ")";
            } else {
                // El mensaje NO se borra, solo se avisa de que falta el archivo
                texto = texto + "\n      [ADJUNTO NO DISPONIBLE] " + adjunto.getNombre()
                        + " (el archivo ya no existe)";
            }
        }
        return texto;
    }
    // Devuelve cuántos mensajes ha enviado cada usuario en la conversación.
    public Map<String, Long> estadisticas(String usuarioA, String usuarioB) throws JAXBException {
        List<Mensaje> conversacion = consultarConversacion(usuarioA, usuarioB);
        return analizador.mensajesPorUsuario(conversacion);
    }
    // Devuelve el resumen completo de la conversación en forma de texto.
    public String generarResumen(String usuarioA, String usuarioB) throws JAXBException {
        List<Mensaje> conversacion = consultarConversacion(usuarioA, usuarioB);
        // Usamos los nombres tal y como están registrados (por si escribieron "ANA" en vez de "ana")
        String a = buscarNombreRegistrado(usuarioA);
        String b = buscarNombreRegistrado(usuarioB);
        return analizador.generarResumen(conversacion, a, b);
    }
    // Construye la ruta del fichero a exportar a partir del nombre que elige el usuario.
    // El fichero se guardará dentro de la carpeta "exportaciones".
    public Path crearRutaExportacion(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del fichero no puede estar vacío.");
        }
        nombre = nombre.trim();
        // Solo permitimos letras, números, espacios, guiones y guiones bajos
        if (!nombre.matches("[\\p{L}\\p{N}_ -]+")) {
            throw new IllegalArgumentException(
                    "Nombre no válido. Usa solo letras, números, espacios, guiones y guiones bajos.");
        }
        return Paths.get("exportaciones", nombre + ".txt");
    }
    // Exporta la conversación a un archivo de texto plano.
    public void exportarConversacion(String usuarioA, String usuarioB, Path destino)
            throws JAXBException, IOException {
        List<Mensaje> conversacion = consultarConversacion(usuarioA, usuarioB);
        String a = buscarNombreRegistrado(usuarioA);
        String b = buscarNombreRegistrado(usuarioB);
        // Cabecera del archivo
        List<String> lineas = new ArrayList<>();
        lineas.add("CONVERSACIÓN ENTRE " + a.toUpperCase() + " Y " + b.toUpperCase());
        lineas.add("Exportada el " + LocalDateTime.now().withNano(0).toString().replace("T", " "));
        lineas.add("Total de mensajes: " + conversacion.size());
        lineas.add("----------------------------------------");
        // Con un Stream convertimos cada mensaje en su línea de texto
        lineas.addAll(conversacion.stream()
                .map(this::formatearMensaje)
                .collect(Collectors.toList()));
        // Si la carpeta "exportaciones" no existe, la creamos
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        // Escribimos todas las líneas en el archivo (con UTF-8 para que salgan bien las tildes)
        Files.write(destino, lineas, StandardCharsets.UTF_8);
    }
    // Envía un mensaje con un archivo adjunto.
    public void enviarMensajeConAdjunto(String emisor, String destinatario, String contenido,Path archivoOrigen) throws JAXBException, IOException {
        //Validar los datos y crear el mensaje
        Mensaje mensaje = crearMensaje(emisor, destinatario, contenido);
        //Validar y copiar el archivo a media/, y guardar sus datos en el mensaje
        adjuntarArchivo(mensaje, archivoOrigen);
        //Guardar el mensaje (con la referencia al adjunto) en el XML
        guardarMensaje(mensaje);
    }
    // Valida el archivo, lo copia a media/ y anota sus datos en el mensaje.
    public void adjuntarArchivo(Mensaje mensaje, Path archivoOrigen) throws IOException {
        Adjunto adjunto = gestorAdjuntos.guardarAdjunto(archivoOrigen);
        mensaje.setAdjunto(adjunto);
    }
    // Busca dentro de una conversación el mensaje con ese id y comprueba que tiene adjunto.
    public Mensaje buscarMensajeConAdjunto(List<Mensaje> conversacion, int id) {
        for (Mensaje m : conversacion) {
            if (m.getId() == id) {
                if (m.getAdjunto() == null) {
                    throw new IllegalArgumentException("El mensaje #" + id + " no tiene adjunto.");
                }
                return m;
            }
        }
        throw new IllegalArgumentException("No hay ningún mensaje #" + id + " en esta conversación.");
    }
    // Abre el adjunto de un mensaje con el programa del ordenador.
    public void abrirAdjunto(Mensaje mensaje) throws IOException {
        gestorAdjuntos.abrir(mensaje.getAdjunto());
    }
    // Devuelve la ruta donde se exportaría el adjunto de un mensaje.
    public Path rutaExportacionAdjunto(Mensaje mensaje) {
        return gestorAdjuntos.rutaExportacion(mensaje.getAdjunto());
    }
    // Exporta el adjunto de un mensaje al destino indicado.
    public void exportarAdjunto(Mensaje mensaje, Path destino) throws IOException {
        gestorAdjuntos.exportar(mensaje.getAdjunto(), destino);
    }
}