package com.vacation_bot.core.services;

import com.vacation_bot.domain.models.UserModel;
import me.ramswaroop.jbot.core.slack.models.User;

/**
 * Responsible for managing user data.
 */
public interface UserPort {

    /**
     * Saves user to the system.
     * @param user the user to save.
     * @return saved user.
     */
    UserModel save( final User user );

    /**
     * Returns the user from the system.
     * If user doesn't exist in the system yet, it will be created.
     * @param userId the user unique identifier.
     * @return the user model.
     */
    UserModel getUser(final String userId );
}
