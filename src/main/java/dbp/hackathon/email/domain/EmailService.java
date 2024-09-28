package dbp.hackathon.email.domain;

import dbp.hackathon.Ticket.Ticket;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class EmailService {

    // Método para leer el archivo HTML y reemplazar los placeholders
    private String readHtmlTemplate(String filePath, Map<String, String> replacements) throws IOException {
        // Leer el archivo HTML desde el classpath
        Path path = new ClassPathResource(filePath).getFile().toPath();
        String content = Files.readString(path);

        // Reemplazar los placeholders con los valores reales
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return content;
    }

    // Método para enviar un correo con el código QR
    public void sendConfirmationEmail(String toEmail, Ticket ticket, String qrCodeUrl) throws IOException {
        // Definir el archivo HTML de la plantilla
        String templatePath = "templates/mail.html";

        // Mapear los valores dinámicos para reemplazar en la plantilla
        Map<String, String> replacements = Map.of(
                "nombre", ticket.getEstudiante().getName(),
                "nombrePelicula", ticket.getFuncion().getNombre(),
                "fechaFuncion", ticket.getFuncion().getFecha().toString(),
                "cantidadEntradas", String.valueOf(ticket.getCantidad()),
                "precioTotal", String.valueOf(ticket.getCantidad() * ticket.getFuncion().getPrecio()),
                "qr", qrCodeUrl
        );

        // Leer y personalizar la plantilla HTML
        String body = readHtmlTemplate(templatePath, replacements);

        // Lógica para enviar el correo usando JavaMail o cualquier otro servicio de correo
        System.out.println("Correo enviado a " + toEmail + " con el siguiente contenido: \n" + body);
    }
}
