package com.vacation_bot.core.services

import com.vacation_bot.AbstractSpockUnitTest
import com.vacation_bot.domain.models.UserModel
import com.vacation_bot.domain.models.VacationTotalModel
import com.vacation_bot.gateway.outbound.slack.SlackApiPort
import com.vacation_bot.repositories.DefaultRepositoryFactory
import com.vacation_bot.repositories.UserModelRepository
import com.vacation_bot.repositories.VacationTotalModelRepository
import me.ramswaroop.jbot.core.slack.models.Profile
import me.ramswaroop.jbot.core.slack.models.User
import spock.lang.Shared

/**
 * Unit level testing of the {@link UserService}.
 */
class UserServiceUnitTest extends AbstractSpockUnitTest {

    def vacationTotalRepository = Mock( VacationTotalModelRepository )
    def userModelRepository = Mock( UserModelRepository )
    def slackApi = Mock( SlackApiPort )
    def factory = new DefaultRepositoryFactory( [vacationTotalRepository, userModelRepository] )
    def sut = new UserService( factory, slackApi )

    @Shared
    def externalUser = new User( id: 'some-id',
            name: 'my-name',
            profile: new Profile( email: 'valid@gmail.com' ) )

    @Shared
    def internalUser = new UserModel( id: externalUser.id,
            name: externalUser.name,
            email: externalUser.profile.email )

    def 'exercise user save'() {
        when: 'the exercised method is called'
        def result = sut.save( externalUser )

        then: 'the repository is called as expected'
        1 * userModelRepository.save( internalUser ) >> internalUser

        and: 'the user is returned as expected'
        result == internalUser
    }

    def 'exercise user retrieval with one existed in the system'() {
        given: 'valid user id'
        def userId = 'valid-user-id'

        and: 'expected user model'
        def expectedUser = new UserModel( id: userId )

        when: 'the exercised method is called'
        def result = sut.getUser( userId )

        then: 'the repository is called as expected'
        1 * userModelRepository.findById( userId ) >> Optional.of( expectedUser )

        and: 'the user is returned as expected'
        result == expectedUser
    }

    def 'exercise user retrieval with no one in the system'() {
        given: 'valid user id'
        def userId = 'valid-user-id'

        and: 'the expected vacation record'
        def vacationTotalRecord = new VacationTotalModel( userId: userId,
                vacationTotal: UserService.DEFAULT_VACATION_TOTAL_DAYS,
                year: Calendar.getInstance().get( Calendar.YEAR ) )

        when: 'the exercised method is called'
        def result = sut.getUser( userId )

        then: 'the repository is called as expected'
        1 * userModelRepository.findById( userId ) >> Optional.empty()

        and: 'the external call is made as expected'
        1 * slackApi.getUser( userId ) >> externalUser

        and: 'the total vacation record is created in the system as expected'
        1 * vacationTotalRepository.save( vacationTotalRecord )

        and: 'the user is saved to the system'
        1 * userModelRepository.save( internalUser ) >> internalUser

        and: 'the user is returned as expected'
        result == internalUser
    }
}
