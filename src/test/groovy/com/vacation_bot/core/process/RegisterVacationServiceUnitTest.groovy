package com.vacation_bot.core.process

import com.vacation_bot.AbstractSpockUnitTest
import com.vacation_bot.domain.CustomizedSentence
import com.vacation_bot.domain.models.UserModel
import com.vacation_bot.domain.models.VacationModel
import com.vacation_bot.domain.models.VacationTotalModel
import com.vacation_bot.repositories.DefaultRepositoryFactory
import com.vacation_bot.repositories.UserModelRepository
import com.vacation_bot.repositories.VacationModelRepository
import com.vacation_bot.repositories.VacationTotalModelRepository
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Unit level testing of the {@link RegisterVacationService}.
 */
class RegisterVacationServiceUnitTest extends AbstractSpockUnitTest {

    def inputUserName = 'Alex'
    def inputStartDate = '02.10.17'
    def inputEndDate = '20.10.17'

    @Shared
    def validUser = Optional.of( new UserModel( id: '1', name: 'Alex', email: 'alex@mail.com' ) )

    @Shared
    def validVacationTotal1 = new VacationTotalModel( userId: validUser.get().getId(), vacationTotal: 15, year: 2017 )

    @Shared
    def validVacationTotal2 = new VacationTotalModel( userId: validUser.get().getId(), vacationTotal: 25, year: 2017 )

    @Unroll
    def 'exercise registerVacation #description'() {
        given: 'valid subject under test'
        def userModelRepository = Mock( UserModelRepository )
        def vacationTotalRepository = Mock( VacationTotalModelRepository )
        def vacationModelRepository = Mock( VacationModelRepository )
        def factory = new DefaultRepositoryFactory( [userModelRepository, vacationTotalRepository, vacationModelRepository] )
        def sut = new RegisterVacationService( factory )

        when: 'the exercised method is called'
        def result = sut.registerVacation( new CustomizedSentence( persons: [inputUserName], dates: [inputStartDate, inputEndDate] ) )

        then: 'repositories return expected values'
        1 * userModelRepository.findById( inputUserName ) >> user
        1 * vacationTotalRepository.findByUserIdAndYear( user.get().id, Calendar.getInstance().get( Calendar.YEAR ) ) >> vacationTotal

        and: 'documents are saved to the database as expected'
        expectToCreateVacation * vacationTotalRepository.save( !null as VacationTotalModel )
        expectToCreateVacation * vacationModelRepository.save( !null as VacationModel )

        result.currentResponse == expectedResult

        where:
        user        ||   vacationTotal        || expectToCreateVacation  |  expectedResult                                                                                                        | description
        validUser   ||   validVacationTotal1  || 0                       |  "You can not receive vacation. You have 15 days."                                                                     | 'with no vacation days left'
        validUser   ||   validVacationTotal2  || 1                       |  "The registration of your vacation from 2017-10-02 to 2017-10-20 was successfully completed! You have left 6 days"    | 'with happy path'
    }
}
