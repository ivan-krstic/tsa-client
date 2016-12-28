package me.krstic.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.krstic.service.TimestampService;
import me.krstic.vo.ServiceResponse;

@RestController
public class TimestampController {
	
	private static final Logger LOG = LoggerFactory.getLogger(TimestampController.class);

	private TimestampService timestampService;
	
	@Autowired
	public TimestampController(TimestampService timestampService) {
		this.timestampService = timestampService;
	}

	@RequestMapping(value = "/timestamp", params = {"id"}, method = RequestMethod.GET)
	public ResponseEntity<ServiceResponse> getTimestamp(@RequestParam(name="id") Long id) {
		ServiceResponse serviceResponse = timestampService.getTimeStampToken(id);
		
		return new ResponseEntity<>(serviceResponse, HttpStatus.valueOf(serviceResponse.getCode()));
	}

}
