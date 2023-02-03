package com.example.gallery.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.gallery.backend.entity.Cart;
import com.example.gallery.backend.entity.Item;
import com.example.gallery.backend.repository.CartRepository;
import com.example.gallery.backend.repository.ItemRepository;
import com.example.gallery.backend.service.JwtService;

@RestController
public class CartController {
	
	@Autowired
	CartRepository cartRepository;
	
	@Autowired
	JwtService jwtService;
	
	@Autowired
	ItemRepository itemRepository;
	
	@GetMapping("/api/cart/items")
	public ResponseEntity getCartItems(@CookieValue(value = "token", required = false) String token) {
		
		if (!jwtService.isValid(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		
		int memberId = jwtService.getId(token);
		List<Cart> carts = cartRepository.findByMemberId(memberId);
		List<Integer> itemIds = carts.stream().map(Cart::getItemId).toList();
		
		List<Item> items = itemRepository.findByIdIn(itemIds);
		
		return new ResponseEntity<>(items, HttpStatus.OK);
	}
	
	@PostMapping("/api/cart/items/{itemId}")
	public ResponseEntity pushCartItem(@PathVariable("itemId") int itemId, @CookieValue(value="token", required = false) String token) {
		
		if (!jwtService.isValid(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		
		int memberId = jwtService.getId(token);
		Cart cart = cartRepository.findByMemberIdAndItemId(memberId, itemId);
		
		if (cart == null) {
			Cart newCart = new Cart();
			newCart.setMemberId(memberId);
			newCart.setItemId(itemId);
			cartRepository.save(newCart);
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@DeleteMapping("/api/cart/items/{itemId}")
	public ResponseEntity removeCartItem(@PathVariable("itemId") int itemId, @CookieValue(value="token", required = false) String token) {
		
		if (!jwtService.isValid(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		
		int memberId = jwtService.getId(token);
		Cart cart = cartRepository.findByMemberIdAndItemId(memberId, itemId);
		
		cartRepository.delete(cart);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
