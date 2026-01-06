package br.eng.eaa.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Named("notify")
public class NotifyCriticalHandler implements RequestHandler<SNSEvent, Void> {

    @Inject
    SesClient sesClient;

    @ConfigProperty(name = "email.from")
    String from;

    @ConfigProperty(name = "email.to")
    String to;

    @Override
    public Void handleRequest(SNSEvent event, Context context) {

        if (event == null || event.getRecords() == null) {
            context.getLogger().log("AVISO: Evento SNS recebido é nulo ou não possui registros.");
            return null;
        }

        for (SNSEvent.SNSRecord record : event.getRecords()) {

            if (record.getSNS() == null || record.getSNS().getMessage() == null) {
                context.getLogger().log("Registro SNS sem mensagem interna.");
                continue;
            }
            String message = record.getSNS().getMessage();

            try {
                sesClient.sendEmail(SendEmailRequest.builder()
                        .source(from)
                        .destination(Destination.builder().toAddresses(to).build())
                        .message(Message.builder()
                                .subject(Content.builder().data("Feedback Crítico Recebido").build())
                                .body(Body.builder()
                                        .text(Content.builder().data(message).build())
                                        .build())
                                .build())
                        .build());
                context.getLogger().log("E-mail enviado com sucesso.");
            } catch (Exception e) {
                context.getLogger().log("ERRO ao enviar e-mail: " + e.getMessage());
            }
        }
        return null;
    }
}


