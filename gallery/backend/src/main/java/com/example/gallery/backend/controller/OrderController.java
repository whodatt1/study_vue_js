package com.example.gallery.backend.controller;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.gallery.backend.dto.OrderDto;
import com.example.gallery.backend.entity.Order;
import com.example.gallery.backend.repository.CartRepository;
import com.example.gallery.backend.repository.OrderRepository;
import com.example.gallery.backend.service.JwtService;

@RestController
public class OrderController {
	
	@Autowired
	JwtService jwtService;
	
	@Autowired
	OrderRepository orderRepository;
	
	@Autowired
	CartRepository cartRepository;
	
	@GetMapping("/api/orders")
	public ResponseEntity getOrder(@CookieValue(value="token", required = false) String token) {
		
		if (!jwtService.isValid(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		int memberId = jwtService.getId(token);
		List<Order> orders = orderRepository.findByMemberIdOrderByIdDesc(memberId);
		return new ResponseEntity<>(orders, HttpStatus.OK);
	}
	
	@Transactional
	@PostMapping("/api/orders")
	public ResponseEntity pushOrder(@RequestBody OrderDto dto, @CookieValue(value="token", required = false) String token) {
		
		if (!jwtService.isValid(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		
		int memberId = jwtService.getId(token);
		Order newOrder = new Order();
		newOrder.setMemberId(memberId);
		newOrder.setName(dto.getName());
		newOrder.setAddress(dto.getAddress());
		newOrder.setPayment(dto.getPayment());
		newOrder.setCardNumber(dto.getCardNumber());
		newOrder.setItems(dto.getItems());
		
		orderRepository.save(newOrder);
		cartRepository.deleteByMemberId(memberId);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
