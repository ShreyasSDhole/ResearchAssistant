package com.research.assistant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/research")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class Research_Controller 
{
	private final Research_Service researchService;

	@PostMapping("/process")
	public ResponseEntity<String> pressContent(@RequestBody Research_Request request)
	{
		String result = researchService.processContent(request);
		return ResponseEntity.ok(result);
	}
}
