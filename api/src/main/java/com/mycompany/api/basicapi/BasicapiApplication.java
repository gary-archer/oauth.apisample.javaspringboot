package com.mycompany.api.basicapi;

import com.mycompany.api.basicapi.plumbing.startup.ApplicationInitializer;
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
		new SpringApplicationBuilder(BasicapiApplication.class)
				.initializers(new ApplicationInitializer())
				.run(args);
	}
}
