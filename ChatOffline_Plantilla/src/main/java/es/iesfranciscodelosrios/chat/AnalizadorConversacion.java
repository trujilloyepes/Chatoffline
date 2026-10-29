package es.iesfranciscodelosrios.chat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AnalizadorConversacion {
    // Cuenta cuántos mensajes hay en total.
    public long contarMensajes(List<Mensaje> mensajes) {
        // stream() convierte la lista en un Stream; count() cuenta los elementos
        return mensajes.stream().count();
    }
    // Cuenta cuántos mensajes ha enviado cada usuario.
    // Resultado de ejemplo: {ana=5, luis=3}
    public Map<String, Long> mensajesPorUsuario(List<Mensaje> mensajes) {
        return mensajes.stream()
                // groupingBy agrupa los mensajes por emisor, y counting() cuenta cada grupo.
                // TreeMap::new hace que el resultado salga ordenado alfabéticamente.
                .collect(Collectors.groupingBy(Mensaje::getEmisor, TreeMap::new, Collectors.counting()));
    }
    // Devuelve las palabras más repetidas, de más a menos.
    // Solo cuenta palabras de 4 o más letras, para ignorar "de", "que", "el"...
    public List<Map.Entry<String, Long>> palabrasMasComunes(List<Mensaje> mensajes, int cuantas) {
        return mensajes.stream()
                //De cada mensaje nos quedamos con su texto en minúsculas
                .map(m -> m.getContenido().toLowerCase())
                //Cada texto se parte en palabras (separando por todo lo que no sea letra o número)
                //   flatMap junta todas las palabras de todos los mensajes en un solo Stream
                .flatMap(texto -> Arrays.stream(texto.split("[^\\p{L}\\p{N}]+")))
                //Nos quedamos solo con las palabras largas
                .filter(palabra -> palabra.length() >= 4)
                //Agrupamos las palabras iguales y las contamos
                .collect(Collectors.groupingBy(palabra -> palabra, Collectors.counting()))
                //Pasamos el resultado a una lista de pares (palabra, veces)
                .entrySet().stream()
                //Ordenamos: primero las que más se repiten; si empatan, por orden alfabético
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.<String, Long>comparingByKey()))
                //Nos quedamos con las primeras
                .limit(cuantas)
                .collect(Collectors.toList());
    }
    // Devuelve el mensaje con más caracteres.
    // Devuelve un Optional porque si la lista está vacía no hay mensaje más largo.
    public Optional<Mensaje> mensajeMasLargo(List<Mensaje> mensajes) {
        return mensajes.stream()
                .max(Comparator.comparingInt(m -> m.getContenido().length()));
    }
    // Calcula la media de palabras por mensaje.
    public double mediaPalabrasPorMensaje(List<Mensaje> mensajes) {
        return mensajes.stream()
                // Para cada mensaje contamos sus palabras (separando por espacios)
                .mapToInt(m -> m.getContenido().trim().split("\\s+").length)
                .average()
                .orElse(0);
    }
    // Devuelve la hora del día (0 a 23) en la que se enviaron más mensajes,
    // junto con cuántos mensajes fueron.
    public Optional<Map.Entry<Integer, Long>> horaMasActiva(List<Mensaje> mensajes) {
        return mensajes.stream()
                // Agrupamos por la hora de la fecha del mensaje y contamos cada grupo
                .collect(Collectors.groupingBy(
                        m -> LocalDateTime.parse(m.getFecha()).getHour(),
                        Collectors.counting()))
                .entrySet().stream()
                // Nos quedamos con la hora que tiene el número más alto
                .max(Map.Entry.comparingByValue());
    }
    // Cuenta cuántos mensajes llevan un archivo adjunto.
    public long contarAdjuntos(List<Mensaje> mensajes) {
        return mensajes.stream()
                .filter(m -> m.getAdjunto() != null)
                .count();
    }
    // Junta todas las estadísticas en un texto listo para mostrar por pantalla.
    public String generarResumen(List<Mensaje> mensajes, String usuarioA, String usuarioB) {
        StringBuilder texto = new StringBuilder();
        texto.append("===== RESUMEN: ").append(usuarioA).append(" y ").append(usuarioB).append(" =====\n");
        // Si no hay mensajes no hay nada que calcular
        if (mensajes.isEmpty()) {
            texto.append("Todavía no hay mensajes entre estos usuarios.\n");
            return texto.toString();
        }
        long total = contarMensajes(mensajes);
        texto.append("Total de mensajes: ").append(total).append("\n");
        // Los mensajes están en orden, así que el primero es el más antiguo y el último el más reciente
        texto.append("Primer mensaje: ").append(mensajes.get(0).getFecha().replace("T", " ")).append("\n");
        texto.append("Último mensaje: ").append(mensajes.get(mensajes.size() - 1).getFecha().replace("T", " ")).append("\n");
        // Mensajes por usuario, con porcentaje
        texto.append("\nMensajes por usuario:\n");
        for (Map.Entry<String, Long> entrada : mensajesPorUsuario(mensajes).entrySet()) {
            long porcentaje = Math.round(entrada.getValue() * 100.0 / total);
            texto.append("  - ").append(entrada.getKey()).append(": ")
                    .append(entrada.getValue()).append(" (").append(porcentaje).append("%)\n");
        }
        // Media de palabras
        texto.append(String.format("%nMedia de palabras por mensaje: %.1f%n", mediaPalabrasPorMensaje(mensajes)));
        // Mensaje más largo (aquí sabemos que la lista no está vacía, así que get() es seguro)
        Mensaje largo = mensajeMasLargo(mensajes).get();
        texto.append("Mensaje más largo (").append(largo.getContenido().length()).append(" caracteres, de ")
                .append(largo.getEmisor()).append("): ").append(largo.getContenido()).append("\n");
        // Hora con más actividad
        Map.Entry<Integer, Long> hora = horaMasActiva(mensajes).get();
        texto.append(String.format("Hora con más actividad: %02d:00 - %02d:59 (%d mensajes)%n",
                hora.getKey(), hora.getKey(), hora.getValue()));
        // Adjuntos
        texto.append("Mensajes con adjunto: ").append(contarAdjuntos(mensajes)).append("\n");
        // Palabras más comunes
        texto.append("\nPalabras más repetidas (4 o más letras):\n");
        List<Map.Entry<String, Long>> palabras = palabrasMasComunes(mensajes, 5);
        if (palabras.isEmpty()) {
            texto.append("  (ninguna todavía)\n");
        } else {
            for (Map.Entry<String, Long> p : palabras) {
                texto.append("  - ").append(p.getKey()).append(": ").append(p.getValue()).append(" veces\n");
            }
        }
        return texto.toString();
    }
}