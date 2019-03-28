package com.vacation_bot;

import com.mongodb.MongoClient;
import com.vacation_bot.core.classification.ClassificationService;
import com.vacation_bot.core.customization.CustomizationService;
import com.vacation_bot.core.process.RegisterVacationService;
import com.vacation_bot.core.process.RequestDaysLeftService;
import com.vacation_bot.core.process.RequestVacationListService;
import com.vacation_bot.core.process.UnknownRequestService;
import com.vacation_bot.core.services.UserPort;
import com.vacation_bot.core.services.UserService;
import com.vacation_bot.core.validation.RegisterVacationValidator;
import com.vacation_bot.core.validation.RequestVacationListValidator;
import com.vacation_bot.core.words.WordsService;
import com.vacation_bot.gateway.outbound.slack.SlackApiPort;
import com.vacation_bot.repositories.DefaultRepositoryFactory;
import com.vacation_bot.repositories.RepositoryFactory;
import com.vacation_bot.shared.ApplicationProperties;
import com.vacation_bot.shared.logging.LoggingAwareBeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ScriptOperations;
import org.springframework.data.mongodb.core.script.ExecutableMongoScript;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication( scanBasePackages = {"me.ramswaroop.jbot", "com.vacation_bot"})
@ImportResource( "classpath:META-INF.spring/application-context.xml" )
@EnableMongoRepositories( basePackages = "com.vacation_bot.repositories" )
@EnableConfigurationProperties( ApplicationProperties.class )
@EnableCaching
public class VacationBotApplication {

	public static void main( String[] args ) {
		SpringApplication.run( VacationBotApplication.class, args );
	}

	@Bean
	public MongoClient mongoClient() {
		return new MongoClient("localhost" );
	}

	@Bean
	public MongoTemplate mongoTemplate() {
		return new MongoTemplate( mongoClient(), "devbot" );
	}

	@Bean
	public RestOperations restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public DefaultRepositoryFactory repositoryFactory( List<MongoRepository> repositories ) {
		return new DefaultRepositoryFactory( repositories );
	}

	@Bean
	public UserPort userPort( RepositoryFactory repositoryFactory, SlackApiPort slackApiPort ) {
		return new UserService( repositoryFactory, slackApiPort );
	}

	@Bean
	public CustomizationService customizationService( RepositoryFactory repositoryFactory ) {
		return new CustomizationService( repositoryFactory );
	}

	@Bean
	public WordsService wordsService( RepositoryFactory repositoryFactory ) {
		return new WordsService( repositoryFactory );
	}

	@Bean
	public ClassificationService classificationService( WordsService wordsService ) {
		return new ClassificationService( wordsService );
	}

	@Bean
	public RegisterVacationService registerVacationService( final RepositoryFactory repositoryFactory ) {
		return new RegisterVacationService( repositoryFactory );
	}

	@Bean
	public RequestDaysLeftService requestDaysLeftService( final RepositoryFactory factory ) {
		return new RequestDaysLeftService( factory );
	}

	@Bean
	public RequestVacationListService requestVacationListService( final RepositoryFactory factory ) {
		return new RequestVacationListService( factory );
	}

	@Bean
	public UnknownRequestService unknownRequestService( final RepositoryFactory factory ) {
		return new UnknownRequestService( factory );
	}

	// ====== FILTERS
	@Bean
	public RequestVacationListValidator requestVacationListValidator() {
		return new RequestVacationListValidator();
	}

	@Bean
	public RegisterVacationValidator registerVacationValidator() {
		return new RegisterVacationValidator();
	}

	@Bean
	public LoggingAwareBeanPostProcessor loggingAwareBeanPostProcessor() {
		return new LoggingAwareBeanPostProcessor();
	}

	@EventListener( ApplicationReadyEvent.class )
	public void loadInitialDataToTheDatabase() {
		ScriptOperations scriptOperations = mongoTemplate().scriptOps();
		try {
			Path path = Paths.get( getClass().getClassLoader().getResource( "sentences.txt" ).toURI() );
			StringBuilder data = new StringBuilder();
			Stream<String> lines = Files.lines( path );
			lines.forEach( line -> data.append( line ).append( "" ) );
			lines.close();
			String readyData = data.toString();
			ExecutableMongoScript script = new ExecutableMongoScript( readyData );
			scriptOperations.execute( script );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
}
