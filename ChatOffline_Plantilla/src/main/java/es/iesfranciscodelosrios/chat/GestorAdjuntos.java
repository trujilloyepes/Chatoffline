package es.iesfranciscodelosrios.chat;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GestorAdjuntos {
    // Carpeta donde se guardan los adjuntos
    private static final String CARPETA_MEDIA = "media";
    // Tamaño máximo permitido: 5 MB (5 * 1024 * 1024 bytes)
    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024;
    // Extensiones permitidas
    private static final List<String> EXTENSIONES_PERMITIDAS =
            List.of("jpg", "jpeg", "png", "gif", "pdf", "txt");
    // Comprueba si el archivo de un adjunto sigue existiendo en la carpeta media/.
    public boolean existe(Adjunto adjunto) {
        return Files.exists(Paths.get(adjunto.getRuta()));
    }
    // Comprueba que el archivo se puede adjuntar.
    // Si algo falla, lanza un error con un mensaje que explica el motivo.
    public void validar(Path origen) throws IOException {
        //El archivo tiene que existir y ser un archivo (no una carpeta)
        if (origen == null || !Files.exists(origen) || !Files.isRegularFile(origen)) {
            throw new IllegalArgumentException("El archivo no existe: " + origen);
        }
        //La extensión tiene que estar en la lista de permitidas
        String extension = obtenerExtension(origen.getFileName().toString());
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException("Extensión no permitida. Solo se admiten: "
                    + EXTENSIONES_PERMITIDAS);
        }
        //El tamaño no puede pasar del máximo
        if (Files.size(origen) > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo es demasiado grande. El máximo es 5 MB.");
        }
    }
    // Valida el archivo, lo copia a media/ y devuelve sus datos (nombre, ruta, tamaño y tipo)
    // para guardarlos en el XML.
    public Adjunto guardarAdjunto(Path origen) throws IOException {
        validar(origen);
        // Si la carpeta media/ no existe, la creamos
        Files.createDirectories(Paths.get(CARPETA_MEDIA));
        // Le ponemos delante la hora en milisegundos para que dos archivos con el mismo
        String nombreOriginal = origen.getFileName().toString();
        String nombreGuardado = System.currentTimeMillis() + "_" + nombreOriginal;
        Path destino = Paths.get(CARPETA_MEDIA, nombreGuardado);
        // Copiamos el archivo
        copiarArchivo(origen, destino);
        // Devolvemos los datos del adjunto
        return new Adjunto(
                nombreOriginal,
                CARPETA_MEDIA + "/" + nombreGuardado,
                Files.size(destino),
                obtenerExtension(nombreOriginal));
    }
    // Copia un archivo byte a byte usando InputStream y OutputStream.
    public void copiarArchivo(Path origen, Path destino) throws IOException {
        try (InputStream entrada = new FileInputStream(origen.toFile());
             OutputStream salida = new FileOutputStream(destino.toFile())) {
            // Leemos el archivo en trozos de 4096 bytes
            byte[] buffer = new byte[4096];
            int leidos;
            // read() devuelve cuántos bytes ha leído, y -1 cuando se acaba el archivo
            while ((leidos = entrada.read(buffer)) != -1) {
                // Escribimos solo los bytes que se han leído de verdad
                salida.write(buffer, 0, leidos);
            }
        }
    }
    // Abre el adjunto con el programa que tenga el ordenador para ese tipo de archivo.
    public void abrir(Adjunto adjunto) throws IOException {
        File archivo = new File(adjunto.getRuta()).getAbsoluteFile();
        if (!archivo.exists()) {
            throw new IllegalArgumentException("El archivo adjunto ya no existe en la carpeta media.");
        }
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Este equipo no permite abrir archivos desde Java.");
        }
        Desktop.getDesktop().open(archivo);
    }
    // Devuelve la ruta donde se exportará un adjunto: exportaciones/adjuntos/nombre
    public Path rutaExportacion(Adjunto adjunto) {
        return Paths.get("exportaciones", "adjuntos", adjunto.getNombre());
    }
    // Copia el adjunto desde media/ al destino indicado.
    public void exportar(Adjunto adjunto, Path destino) throws IOException {
        Path origen = Paths.get(adjunto.getRuta());
        if (!Files.exists(origen)) {
            throw new IllegalArgumentException("El archivo adjunto ya no existe en la carpeta media.");
        }
        // Si la carpeta de destino no existe, la creamos
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        copiarArchivo(origen, destino);
    }
    // Devuelve la extensión de un nombre de archivo, en minúsculas y sin el punto.
    private String obtenerExtension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto == -1 || punto == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(punto + 1).toLowerCase();
    }
}
