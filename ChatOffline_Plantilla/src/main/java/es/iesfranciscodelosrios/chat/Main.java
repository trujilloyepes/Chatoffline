package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        GestorChat chat = new GestorChat();
        String opcion = "";
        // El menú se repite hasta que el usuario escribe 0
        while (!opcion.equals("0")) {
            System.out.println();
            System.out.println("=== CHAT OFFLINE ===");
            System.out.println("1. Registrar usuario");
            System.out.println("2. Enviar mensaje");
            System.out.println("3. Consultar conversación");
            System.out.println("4. Estadísticas");
            System.out.println("5. Exportar conversación");
            System.out.println("6. Adjuntar archivo");
            System.out.println("0. Salir");
            System.out.print("Elige una opción: ");
            opcion = teclado.nextLine().trim();
            // try/catch: si algo falla, mostramos el error en vez de cerrar el programa
            try {
                if (opcion.equals("1")) {
                    System.out.print("Nombre del nuevo usuario: ");
                    String nombre = teclado.nextLine();
                    chat.registrarUsuario(nombre);
                    System.out.println("Usuario registrado correctamente.");
                } else if (opcion.equals("2")) {
                    System.out.print("Emisor (quien envía): ");
                    String emisor = teclado.nextLine();
                    System.out.print("Destinatario (quien recibe): ");
                    String destinatario = teclado.nextLine();
                    System.out.print("Mensaje: ");
                    String contenido = teclado.nextLine();
                    chat.enviarMensaje(emisor, destinatario, contenido);
                    System.out.println("Mensaje enviado correctamente.");
                } else if (opcion.equals("3")) {
                    menuConsulta(teclado, chat);
                } else if (opcion.equals("4")) {
                    menuEstadisticas(teclado, chat);
                } else if (opcion.equals("5")) {
                    menuExportar(teclado, chat);

                } else if (opcion.equals("6")) {
                    menuAdjuntar(teclado, chat);
                } else if (opcion.equals("0")) {
                    System.out.println("Hasta pronto.");
                } else {
                    System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException e) {
                // Errores de validación: usuario no registrado, extensión no permitida...
                System.out.println("Error: " + e.getMessage());
            } catch (JAXBException e) {
                // Errores al leer o escribir el XML
                System.out.println("Error con el archivo XML: " + e.getMessage());
            } catch (IOException e) {
                // Errores al leer o escribir archivos (exportar, copiar adjuntos...)
                System.out.println("Error con el archivo: " + e.getMessage());
            }
        }
        teclado.close();
    }
    // Opción 3: elegir dos usuarios y ver su conversación.
    // Si hay adjuntos, permite abrirlos o exportarlos.
    private static void menuConsulta(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        //Mostrar los usuarios que hay para que se pueda elegir
        List<String> usuarios = chat.listarUsuarios();
        if (usuarios.isEmpty()) {
            System.out.println("Todavía no hay usuarios registrados.");
            return;
        }
        System.out.println("Usuarios registrados: " + usuarios);
        //Elegir quién eres y con quién quieres ver la conversación
        System.out.print("Tu usuario: ");
        String yo = teclado.nextLine();
        System.out.print("Ver conversación con: ");
        String otro = teclado.nextLine();
        //Recuperar los mensajes entre los dos (si hay error, sube al catch del main)
        List<Mensaje> conversacion = chat.consultarConversacion(yo, otro);
        //Mostrar el resultado
        System.out.println();
        System.out.println("--- Conversación entre " + yo + " y " + otro + " ---");
        if (conversacion.isEmpty()) {
            System.out.println("No hay mensajes que mostrar.");
        } else {
            for (Mensaje m : conversacion) {
                System.out.println(chat.formatearMensaje(m));
            }
            System.out.println("--- " + conversacion.size() + " mensaje(s) ---");
        }
        //Si hay adjuntos, ofrecer abrirlos o exportarlos
        menuAdjuntos(teclado, chat, conversacion);
    }
    // Permite abrir o exportar un adjunto de la conversación que se acaba de mostrar.
    private static void menuAdjuntos(Scanner teclado, GestorChat chat, List<Mensaje> conversacion)
            throws IOException {
        // Comprobar si algún mensaje tiene adjunto
        boolean hayAdjuntos = false;
        for (Mensaje m : conversacion) {
            if (m.getAdjunto() != null) {
                hayAdjuntos = true;
            }
        }
        if (!hayAdjuntos) {
            return;
        }
        System.out.println();
        System.out.print("Para abrir o exportar un adjunto escribe el número del mensaje "
                + "(por ejemplo 3), o pulsa Enter para salir: ");
        String texto = teclado.nextLine().trim().replace("#", "");
        if (texto.isEmpty()) {
            return;
        }
        // Convertir el texto a número
        int id;
        try {
            id = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            System.out.println("Número no válido.");
            return;
        }
        // Buscar el mensaje (si no existe o no tiene adjunto, salta un error)
        Mensaje mensaje = chat.buscarMensajeConAdjunto(conversacion, id);
        System.out.println("1. Abrir el adjunto");
        System.out.println("2. Exportar el adjunto a la carpeta exportaciones/adjuntos");
        System.out.print("Elige una opción: ");
        String eleccion = teclado.nextLine().trim();
        if (eleccion.equals("1")) {
            chat.abrirAdjunto(mensaje);
            System.out.println("Adjunto abierto.");
        } else if (eleccion.equals("2")) {
            Path destino = chat.rutaExportacionAdjunto(mensaje);
            // Si ya existe, preguntamos antes de sobreescribir
            if (Files.exists(destino)) {
                System.out.print("Ese fichero ya existe. ¿Quieres sobreescribirlo? (s/n): ");
                String respuesta = teclado.nextLine().trim();
                if (!respuesta.equalsIgnoreCase("s")) {
                    System.out.println("Exportación cancelada.");
                    return;
                }
            }
            chat.exportarAdjunto(mensaje, destino);
            System.out.println("Adjunto exportado en: " + destino.toAbsolutePath());

        } else {
            System.out.println("Opción no válida.");
        }
    }
    // Opción 4: pide dos usuarios y muestra el resumen con las estadísticas.
    private static void menuEstadisticas(Scanner teclado, GestorChat chat) throws JAXBException {
        System.out.println("Usuarios registrados: " + chat.listarUsuarios());
        System.out.print("Primer usuario: ");
        String a = teclado.nextLine();
        System.out.print("Segundo usuario: ");
        String b = teclado.nextLine();
        System.out.println();
        System.out.println(chat.generarResumen(a, b));
    }
    // Opción 5: exporta la conversación a un archivo de texto.
    private static void menuExportar(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        System.out.println("Usuarios registrados: " + chat.listarUsuarios());
        System.out.print("Primer usuario: ");
        String yo = teclado.nextLine();
        System.out.print("Segundo usuario: ");
        String otro = teclado.nextLine();
        // Comprobamos ya que los usuarios existen (si no, salta el error y no seguimos)
        chat.consultarConversacion(yo, otro);
        // Elegir el nombre del fichero
        System.out.print("Nombre del fichero (Enter para un nombre automático): ");
        String nombre = teclado.nextLine().trim();
        if (nombre.isEmpty()) {
            nombre = "conversacion_" + yo + "_" + otro;
        }
        Path destino = chat.crearRutaExportacion(nombre);
        // Si ya existe un fichero con ese nombre, preguntamos antes de sobreescribirlo
        if (Files.exists(destino)) {
            System.out.print("Ese fichero ya existe. ¿Quieres sobreescribirlo? (s/n): ");
            String respuesta = teclado.nextLine().trim();
            if (!respuesta.equalsIgnoreCase("s")) {
                System.out.println("Exportación cancelada.");
                return;
            }
        }
        chat.exportarConversacion(yo, otro, destino);
        System.out.println("Conversación exportada en: " + destino.toAbsolutePath());
    }
    // Opción 6: envía un mensaje con un archivo adjunto.
    private static void menuAdjuntar(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        System.out.println("Usuarios registrados: " + chat.listarUsuarios());
        System.out.print("Emisor (quien envía): ");
        String emisor = teclado.nextLine();
        System.out.print("Destinatario (quien recibe): ");
        String destinatario = teclado.nextLine();
        System.out.print("Mensaje que acompaña al archivo: ");
        String contenido = teclado.nextLine();

        System.out.print("Ruta del archivo (por ejemplo C:\\Users\\tuusuario\\Desktop\\foto.png): ");
        // Si pegas la ruta con "Copiar como ruta" de Windows, trae comillas: las quitamos
        String ruta = teclado.nextLine().trim().replace("\"", "");

        chat.enviarMensajeConAdjunto(emisor, destinatario, contenido, Paths.get(ruta));
        System.out.println("Mensaje con adjunto enviado correctamente.");
    }
}