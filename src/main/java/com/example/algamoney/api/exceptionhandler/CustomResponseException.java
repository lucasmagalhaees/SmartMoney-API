package com.example.algamoney.api.exceptionhandler;

import org.springframework.http.HttpStatus;

public class CustomResponseException extends RuntimeException {
	   private HttpStatus status;
	   
	   public CustomResponseException(String mensagem, HttpStatus status) {
	       super(mensagem);
	       this.status = status;
	   }
	   
	   public HttpStatus getStatus() {
	       return this.status;
	   }
	}