package com.example.algamoney.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.repository.lancamento.LancamentosRepositoryQuery;

public interface LancamentosRepository extends JpaRepository<Lancamento, Long>, LancamentosRepositoryQuery {

	public Optional<Lancamento> findByDescricao(String descricao); 
	
	public List<Lancamento> findByPessoa(Pessoa pessoa);
	
}
