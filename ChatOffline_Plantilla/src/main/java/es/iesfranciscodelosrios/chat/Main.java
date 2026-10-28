package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBException;
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
            // try catch: si algo falla, mostramos el error en vez de cerrar el programa
            try {
                if (opcion.equals("1")) {
                    System.out.print("Nombre del nuevo usuario: ");
                    String nombre = teclado.nextLine();
                    chat.registrarUsuario(nombre);System.out.println("Usuario registrado correctamente.");

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
                } else if (opcion.equals("4") || opcion.equals("5") || opcion.equals("6")) {
                    System.out.println("Opción pendiente de implementar.");
                } else if (opcion.equals("0")) {
                    System.out.println("Hasta pronto.");
                } else {
                    System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException e) {
                // Errores de validación: usuario no registrado, mensaje vacío...
                System.out.println("Error: " + e.getMessage());
            } catch (JAXBException e) {
                // Errores al leer o escribir el XML
                System.out.println("Error con el archivo XML: " + e.getMessage());
            }
        }
        teclado.close();
    }
    // Submenú de la opción 3: elegir usuarios y ver la conversación con filtros.
    private static void menuConsulta(Scanner teclado, GestorChat chat) throws JAXBException {
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
        //Elegir qué mostrar
        System.out.println();
        System.out.println("¿Qué quieres ver?");
        System.out.println("1. Toda la conversación");
        System.out.println("2. Solo mensajes que contengan una palabra");
        System.out.println("3. Solo mensajes de un día");
        System.out.print("Elige una opción: ");
        String filtro = teclado.nextLine().trim();
        if (filtro.equals("2")) {
            System.out.print("Palabra a buscar: ");
            String palabra = teclado.nextLine();
            conversacion = chat.filtrarPorPalabra(conversacion, palabra);

        } else if (filtro.equals("3")) {
            System.out.print("Fecha (año-mes-día, por ejemplo 2026-10-28): ");
            String fecha = teclado.nextLine();
            conversacion = chat.filtrarPorFecha(conversacion, fecha);

        } else if (!filtro.equals("1")) {
            System.out.println("Opción no válida, se muestra toda la conversación.");
        }
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
    }
}