package com.vacation_bot.gateway.inbound;

import com.vacation_bot.core.InternalTranslationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( "/api" )
@CrossOrigin( "*" )
public class RestInboundGateway {

    private final InternalTranslationPort internalPort;

    @Autowired
    RestInboundGateway( final InternalTranslationPort anInternalPort ) {
        internalPort = anInternalPort;
    }

    @PostMapping()
    public void getResponse( @RequestBody String requestSentence ) {
        Message<String> message = MessageBuilder.withPayload( requestSentence ).build();
        internalPort.processSentence( message );
    }
}