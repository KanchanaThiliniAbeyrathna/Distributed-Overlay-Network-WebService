package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import db.NetworkService;

/**
 * Created by Thilini on 11/5/2017.
 */

@RestController
@RequestMapping("")
public class WebServiceController {

	@Autowired
	private NetworkService net;

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<String> getAll() {
		return new ResponseEntity<String>("working", HttpStatus.OK);
	}

}
