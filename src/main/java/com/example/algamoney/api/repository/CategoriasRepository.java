package com.example.algamoney.api.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.algamoney.api.model.Categoria;

public interface CategoriasRepository extends JpaRepository<Categoria,Long> {
	
	public Optional<Categoria> findByNome(String nome);
	
	
	

}
