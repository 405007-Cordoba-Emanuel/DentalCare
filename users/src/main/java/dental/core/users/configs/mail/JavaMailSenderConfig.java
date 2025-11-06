package dental.core.users.configs.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.w3c.tidy.Tidy;

import java.util.Properties;

@Configuration
public class JavaMailSenderConfig {
	/**
	 * The host of the mail server.
	 * The value is injected from the application configuration.
	 */
	@Value("${spring.mail.host}")
	private String host;

	/**
	 * The port of the mail server.
	 * The value is injected from the application configuration.
	 */
	@Value("${spring.mail.port}")
	private int port;

	/**
	 * The username for authenticating with the mail server.
	 * The value is injected from the application configuration.
	 */
	@Value("${spring.mail.username}")
	private String username;

	/**
	 * The password for authenticating with the mail server.
	 * The value is injected from the application configuration .
	 */
	@Value("${spring.mail.password}")
	private String password;

	/**
	 * Configures and returns a {@link JavaMailSender} bean.
	 * <p>
	 * This method sets up the {@link JavaMailSenderImpl} with the host, port,
	 * username, and password for the SMTP server.
	 *
	 * @return a configured {@link JavaMailSender} instance.
	 */
	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setUsername(username);
		mailSender.setPassword(password);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", "true");

		return mailSender;
	}

	@Bean
	public Tidy tidy() {
		return new Tidy();
	}
}
