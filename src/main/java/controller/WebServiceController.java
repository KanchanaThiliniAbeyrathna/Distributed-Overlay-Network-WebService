package controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import db.NetworkService;
import db.MovieController;
import model.Config;
import model.Node;
import model.SearchResult;
import model.Statistics;

/**
 * Created by Thilini on 11/5/2017.
 */

@RestController
@RequestMapping("")
public class WebServiceController {

	@Autowired
	private NetworkService net;
	final static private Logger logger = Logger.getLogger(WebServiceController.class);

	@Autowired
    private HttpServletRequest httpRequest;
	
	final private MovieController movieController = MovieController.getInstance("../../resources/File Names.txt");
	private List<String> movies;

	public WebServiceController() {
		BasicConfigurator.configure();
		movies = this.movieController.getNodeMovies();
	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<String> getAll() {
		return new ResponseEntity<String>("working", HttpStatus.OK);
	}

	@RequestMapping(value = "register", method = RequestMethod.GET)
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
		Thread thread2 = new Thread() {
            public void run() {
                try {
                    net.update();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        };
        thread2.start();
		return new ResponseEntity<String>("connected", HttpStatus.OK);
	}
	
	@RequestMapping(value = "unregister", method = RequestMethod.GET)
	public ResponseEntity<String> unregister() {
		net.unRegister();
		return new ResponseEntity<String>(Config.USERNAME +" is unregisterd successfully", HttpStatus.OK);
	}
	
	@RequestMapping(value = "leave", method = RequestMethod.GET)
	public ResponseEntity<String> leave() {
		net.send_leave();
		net.unRegister();
		return new ResponseEntity<String>(Config.USERNAME +" is left the network successfully", HttpStatus.OK);
	}
	
	@RequestMapping(value = "neighbors", method = RequestMethod.GET)
	public ResponseEntity<List<Node>> neighbors() {
		List<Node> neighbors= net.getNeighbours();
		return new ResponseEntity<List<Node>>(neighbors, HttpStatus.OK);
	}
	
	@RequestMapping(value = "movies", method = RequestMethod.GET)
	public ResponseEntity<List<String>> movies() {
		return new ResponseEntity<List<String>>(this.movies, HttpStatus.OK);
	}
	
	@RequestMapping(value = "search", method = RequestMethod.GET)
	public ResponseEntity<List<SearchResult>> search(@RequestParam(value = "query", required = true) String query) {
		net.clearSearchResults();
        String movie = query.trim().replace(" ", "_");
        net.startSearch(movie);
//        new java.util.Timer().schedule( 
//			new java.util.TimerTask() {
//				@Override
//				public void run() {
//					logger.info("searching");
//				}
//			}, 60000 
//        );
        try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        List<SearchResult> searchresults=net.getSearchResults();
        if (searchresults.size() > 0){
        	return new ResponseEntity<List<SearchResult>>(searchresults, HttpStatus.OK);
        }else{
        	return new ResponseEntity<List<SearchResult>>(HttpStatus.NOT_FOUND);
        }
	}
	
	@RequestMapping(value = "stats", method = RequestMethod.GET)
	public ResponseEntity<Statistics> stats() {
		Statistics stats= net.getStatistics();
		return new ResponseEntity<Statistics>(stats, HttpStatus.OK);
	}
	
	@RequestMapping(value = "clearstats", method = RequestMethod.GET)
	public ResponseEntity<String> clearstats() {
		net.clearStats();
		return new ResponseEntity<String>("Statistics are cleared", HttpStatus.OK);
	}
	
	@RequestMapping(value = "routingtable", method = RequestMethod.GET)
	public ResponseEntity<String> routingtable() {
		String table= net.routingTable();
		return new ResponseEntity<String>(table, HttpStatus.OK);
	}
}
