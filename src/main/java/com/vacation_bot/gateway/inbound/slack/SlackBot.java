package com.vacation_bot.gateway.inbound.slack;

import com.vacation_bot.core.InternalTranslationPort;
import com.vacation_bot.core.services.UserPort;
import com.vacation_bot.domain.CustomizedSentence;
import com.vacation_bot.domain.models.UserModel;
import me.ramswaroop.jbot.core.common.Controller;
import me.ramswaroop.jbot.core.common.EventType;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class SlackBot extends Bot {

    /**
     * The entry point to the spring integration process.
     */
    private final InternalTranslationPort internalPort;

    /**
     * The service responsible for interactions with user data.
     */
    private final UserPort userService;

    SlackBot( final InternalTranslationPort anInternalPort, final UserPort aUserService ) {
        internalPort = anInternalPort;
        userService = aUserService;
    }

    @Value( "${slackBotToken}" )
    private String slackToken;

    @Controller( events = { EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE } )
    public void onReceiveDM( WebSocketSession session, Event event ) {
        if ( event.getText().contains( "hi" ) ) {
            reply( session, event, new Message( "Hi, I am " + slackService.getCurrentUser().getName() ) );
        }
        else {
            final UserModel user = userService.getUser( event.getUserId() );
            CustomizedSentence inputSentence = new CustomizedSentence();
            inputSentence.setOriginalSentence( event.getText() );
            inputSentence.setUserExternalCode( user.getId() );
            String response = internalPort.processSentence( MessageBuilder.withPayload( inputSentence ).build() )
                                          .getCurrentResponse();
            reply( session, event, new Message( response ) );
        }
    }

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }
}
