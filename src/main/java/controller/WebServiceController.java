package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import db.NetworkService;

/**
 * Created by Thilini on 11/5/2017.
 */

@RestController
@RequestMapping("distibutedsearch")
public class WebServiceController {
	
	@Autowired
	private NetworkService net;
	
	
}
