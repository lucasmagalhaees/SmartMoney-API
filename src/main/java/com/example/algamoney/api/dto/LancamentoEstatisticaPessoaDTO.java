package com.example.algamoney.api.dto;

import com.example.algamoney.api.model.Pessoa;

public class LancamentoEstatisticaPessoaDTO {

	
	private String tipo;
	
	private Pessoa pessoa;
	
	private String total;

	

	public String getTipo() {
		return tipo;
	}



	public void setTipo(String tipo) {
		this.tipo = tipo;
	}



	public Pessoa getPessoa() {
		return pessoa;
	}



	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}



	public String getTotal() {
		return total;
	}



	public void setTotal(String total) {
		this.total = total;
	}



	public LancamentoEstatisticaPessoaDTO() {
		
	}


	
}
