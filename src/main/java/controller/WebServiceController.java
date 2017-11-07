package controller;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import db.NetworkService;
import model.Config;

/**
 * Created by Thilini on 11/5/2017.
 */

@RestController
@RequestMapping("")
public class WebServiceController {

	@Autowired
	private NetworkService net;
	final static private Logger logger = Logger.getLogger(WebServiceController.class);

	public WebServiceController() {
		BasicConfigurator.configure();
	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<String> getAll() {
		return new ResponseEntity<String>("working", HttpStatus.OK);
	}

	@RequestMapping(value = "connect", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> connect(@RequestParam(value = "bootstrap_ip", required = true) String bootstrap_ip,
			@RequestParam(value = "bootstrap_port", required = true) String bootstrap_port,
			@RequestParam(value = "node_ip", required = true) String node_ip,
			@RequestParam(value = "port", required = true) String port,
			@RequestParam(value = "username", required = true) String username) {
		Config.IP = node_ip;
		Config.PORT = Integer.parseInt(port);
		Config.USERNAME = username;
		Config.BOOTSTRAP_IP = bootstrap_ip;
		Config.BOOTSTRAP_PORT = Integer.parseInt(bootstrap_port);
		Thread thread1 = new Thread() {
			public void run() {
				try {
					net.run();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		};
		thread1.start();
		return new ResponseEntity<String>("connected", HttpStatus.OK);
	}
}
