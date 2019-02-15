package com.mycompany.api.basicapi;

import com.ea.async.Async;
import com.mycompany.api.basicapi.framework.errors.ErrorHandler;
import com.mycompany.api.basicapi.startup.ApplicationInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/*
 * The application entry point
 */
@SpringBootApplication
public class BasicapiApplication
{
	/*
	 * The entry point method which starts the app
	 */
	public static void main(String[] args)
	{
		// Initialise the EA library, which allows us to use async await syntax
		Async.init();

		try {
			// Run the app
			new SpringApplicationBuilder(BasicapiApplication.class)
					.initializers(new ApplicationInitializer())
					.run(args);
		}
		catch(Exception ex) {

			var errorHandler = new ErrorHandler();
			errorHandler.handleStartupException(ex);
		}
	}
}
