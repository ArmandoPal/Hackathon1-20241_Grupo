package dbp.hackathon.Ticket;

import dbp.hackathon.Estudiante.Estudiante;
import dbp.hackathon.Estudiante.EstudianteRepository;
import dbp.hackathon.Funcion.Funcion;
import dbp.hackathon.Funcion.FuncionRepository;
import dbp.hackathon.email.domain.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private FuncionRepository funcionRepository;

    @Autowired
    EmailService emailService;

    private static final String QR_CODE_API_URL = "https://api.qrserver.com/v1/create-qr-code/";

    public Ticket createTicket(Long estudianteId, Long funcionId, Integer cantidad) throws IOException, InterruptedException {
        Estudiante estudiante = estudianteRepository.findById(estudianteId).orElse(null);
        Funcion funcion = funcionRepository.findById(funcionId).orElse(null);
        if (estudiante == null || funcion == null) {
            throw new IllegalStateException("Estudiante or Funcion not found!");
        }

        Ticket ticket = new Ticket();
        ticket.setEstudiante(estudiante);
        ticket.setFuncion(funcion);
        ticket.setCantidad(cantidad);
        ticket.setEstado(Estado.VENDIDO);
        ticket.setFechaCompra(LocalDateTime.now());

        // Generar el código QR y almacenarlo en el ticket
        String qrCodeUrl = generateQrCode(ticket.getId().toString());
        ticket.setQr(qrCodeUrl);

        // Guardar el ticket en la base de datos
        Ticket savedTicket = ticketRepository.save(ticket);

        // Enviar correo de confirmación con el código QR
        emailService.sendConfirmationEmail(estudiante.getEmail(), savedTicket, savedTicket.getQr());

        return savedTicket;
    }



    private String generateQrCode(String data) throws IOException, InterruptedException {
        String url = QR_CODE_API_URL + "?size=150x150&data=" + data;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Devolver la URL del código QR generado
        return url;
    }

    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        ticketRepository.deleteById(id);
    }

    public Iterable<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Iterable<Ticket> findByEstudianteId(Long estudianteId) {
        return ticketRepository.findByEstudianteId(estudianteId);
    }

    public void changeState(Long id) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            throw new IllegalStateException("Ticket not found!");
        }
        ticket.setEstado(Estado.CANJEADO);
        ticketRepository.save(ticket);
    }

    public boolean validarTicket(String qrCode) {
        // Buscar el ticket por el código QR
        Ticket ticket = ticketRepository.findByQr(qrCode);
        if (ticket == null || ticket.getEstado() == Estado.CANJEADO) {
            return false; // Ticket no existe o ya ha sido canjeado
        }

        // Cambiar estado a "canjeado"
        ticket.setEstado(Estado.CANJEADO);
        ticketRepository.save(ticket);

        // Enviar un correo de confirmación
        emailService.sendConfirmationEmail(ticket.getEstudiante().getEmail(), ticket, ticket.getQr());

        return true; // El ticket fue validado y canjeado
    }

}
