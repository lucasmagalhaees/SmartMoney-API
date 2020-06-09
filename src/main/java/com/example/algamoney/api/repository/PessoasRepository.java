package com.example.algamoney.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.repository.pessoa.PessoasRepositoryQuery;

public interface PessoasRepository extends JpaRepository<Pessoa, Long>, PessoasRepositoryQuery {

	public Optional<Pessoa> findByNome(String nome);

	@Query(nativeQuery = true, value = "SELECT DISTINCT P.* FROM LANCAMENTO L JOIN PESSOA P"
			+ " ON L.CODIGO_PESSOA = P.CODIGO"
			+ " WHERE L.DATA_VENCIMENTO >= :dataInicio AND L.DATA_VENCIMENTO <= :dataFim")
	public List<Pessoa> buscarPessoasRelatorioLancamento(@Param("dataInicio") LocalDate dataInicio,
			@Param("dataFim") LocalDate dataFim);

}
