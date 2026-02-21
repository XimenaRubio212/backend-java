package com.tastytap.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import java.util.Properties;

public class EmailUtil {
    private static final String REMITENTE = "tu_correo@gmail.com"; 
    private static final String PASSWORD = "tu_clave_app"; 

    public static void enviarFactura(String destinatario, String asunto, String mensaje, byte[] pdfContent) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);

            // Cuerpo del mensaje
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(mensaje);

            // Adjunto del PDF (RF09)
            MimeBodyPart attachmentPart = new MimeBodyPart();
            ByteArrayDataSource ds = new ByteArrayDataSource(pdfContent, "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(ds));
            attachmentPart.setFileName("Factura_TastyTap.pdf");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);
            Transport.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}