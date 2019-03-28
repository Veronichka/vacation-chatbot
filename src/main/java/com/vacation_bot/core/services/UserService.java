package com.vacation_bot.core.services;

import com.vacation_bot.core.BaseService;
import com.vacation_bot.domain.models.UserModel;
import com.vacation_bot.domain.models.VacationTotalModel;
import com.vacation_bot.gateway.outbound.slack.SlackApiPort;
import com.vacation_bot.repositories.RepositoryFactory;
import me.ramswaroop.jbot.core.slack.models.User;

import java.util.Calendar;

/**
 * Responsible for managing user data.
 */
public class UserService extends BaseService implements UserPort {

    /**
     * The default total vacation days.
     */
    private static final int DEFAULT_VACATION_TOTAL_DAYS = 20;

    /**
     * Manages Slack API calls.
     */
    private final SlackApiPort slackApiPort;

    public UserService( final RepositoryFactory factory, final SlackApiPort aSlackApiPort ) {
        super( factory );
        slackApiPort = aSlackApiPort;
    }

    @Override
    public UserModel save( final User user ) {
        final UserModel userModel = new UserModel();
        userModel.setId( user.getId() );
        userModel.setName( user.getName() );
        userModel.setEmail( user.getProfile().getEmail() );
        return getUserModelRepository().save( userModel );
    }

    @Override
    public UserModel getUser( final String userId ) {
        return getUserModelRepository().findById( userId )
                .orElseGet( () -> retrieveUserFromSlack(userId) );
    }

    private UserModel retrieveUserFromSlack( final String userId ) {
        final User slackUser = slackApiPort.getUser( userId );
        createInitialVacationTotal( userId );
        return save( slackUser );
    }

    private void createInitialVacationTotal( final String userId ) {
        VacationTotalModel initialVacationTotal = new VacationTotalModel();
        initialVacationTotal.setUserId( userId );
        initialVacationTotal.setVacationTotal( DEFAULT_VACATION_TOTAL_DAYS );
        initialVacationTotal.setYear( Calendar.getInstance().get( Calendar.YEAR ) );
        getVacationTotalRepository().save( initialVacationTotal );
    }
}
