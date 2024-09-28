package dbp.hackathon.email.event;

import dbp.hackathon.Ticket.Ticket;
import dbp.hackathon.email.domain.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EmailListener {

    @Autowired
    private EmailService emailService;

    // Escuchar el evento de tipo EmailEvent
    @EventListener
    public void handleEmailEvent(EmailEvent event) throws IOException {
        // Obtener el ticket del evento
        Ticket ticket = event.getTicket();

        // Lógica para enviar el correo
        String qrCodeUrl = ticket.getQr();
        emailService.sendConfirmationEmail(
                ticket.getEstudiante().getEmail(),  // Puedes cambiar esto a jorge.martinez@utec.edu.pe si así lo deseas
                ticket,
                qrCodeUrl
        );
    }
}
