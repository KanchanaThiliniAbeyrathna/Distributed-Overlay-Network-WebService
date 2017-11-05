package application;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import db.NetworkService;

/**
 * Created by Thilini on 11/5/2017.
 */

@Configuration
@EnableWebMvc
@ComponentScan({ "db", "model" ,"controller"})
public class AppConfig extends WebMvcConfigurerAdapter{

	@Autowired
	private ServletContext context;
	
	@Bean
	public NetworkService getStudentervice(){
		return new NetworkService();
	}
}