package com.mycompany.api.basicapi;

import com.ea.async.Async;
import com.mycompany.api.basicapi.plumbing.startup.ApplicationInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/*
 * The application entry point
 */
@SpringBootApplication
public class BasicapiApplication {

	/*
	 * The entry point method
	 */
	public static void main(String[] args)
	{
		// Initialise async await processing
		Async.init();

		// Start the application
		new SpringApplicationBuilder(BasicapiApplication.class)
				.initializers(new ApplicationInitializer())
				.run(args);
	}
}
