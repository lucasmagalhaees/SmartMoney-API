package com.example.algamoney.api.repository.lancamento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.example.algamoney.api.dto.LancamentoEstatisticaCategoria;
import com.example.algamoney.api.dto.LancamentoEstatisticaDia;
import com.example.algamoney.api.dto.LancamentoEstatisticaPessoa;
import com.example.algamoney.api.model.Categoria_;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Lancamento_;
import com.example.algamoney.api.model.Pessoa_;
import com.example.algamoney.api.repository.filter.LancamentoFilter;
import com.example.algamoney.api.repository.projection.ResumoLancamento;

public class LancamentosRepositoryImpl implements LancamentosRepositoryQuery {

	@PersistenceContext
	private EntityManager manager;

	// sem a paginacao:
	// List<Lancamento>

	// com a paginacao
	// Page<Lancamento>

	// com a paginacao
//	adicionarRestricoesDePaginacao(query, pageable);
//	return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));

	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {

//		DateTimeFormat(pattern = "yyy-MM-dd")

//		builder se pega, query se cria	
//		manager é o EntityManager
//		manager pega CriteriaBuilder(builder) e CreateQuery
//		builder cria CriteriaQuery(criteria) => o que eu quero devolver
//		criteria.from gera root => é onde eu vou buscar
//		criteria tem acesso a métodos como distinct, orderBy, groupBy, select, where
//		builder tem acesso a métodos como like, between, greaterThan, lessThan, avg, sum, isnull, isTrue, max, min, CONSTRUCT
//		CONSTRUCT constroí o dto que você quer retornar diretamente
//		o CreateQuery do manager vai devolver um TypedQuery
//		voce retornar por padrao a instancia de TypedQuery e dá um getResultList
//		o criteria da um where no array de predicates
//		para construir o predicates vc pega o dto de filtro, o builder e o root
// 		CriteriaBuilder builder = manager.getCriteriaBuilder();

		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);

		criteria.orderBy(builder.asc(root.get("codigo")));

		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);

		TypedQuery<Lancamento> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));

		// sem a paginacao:
		// return query.getResultList();

		// com a paginacao
//		adicionarRestricoesDePaginacao(query, pageable);
//		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));

	}

	@Override
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<ResumoLancamento> criteria = builder.createQuery(ResumoLancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);

		criteria.orderBy(builder.asc(root.get("codigo")));

		criteria.select(builder.construct(ResumoLancamento.class, root.get(Lancamento_.codigo),
				root.get(Lancamento_.descricao), root.get(Lancamento_.dataVencimento),
				root.get(Lancamento_.dataPagamento), root.get(Lancamento_.valor), root.get(Lancamento_.tipo),
				root.get(Lancamento_.categoria).get(Categoria_.nome), root.get(Lancamento_.pessoa).get(Pessoa_.nome)));

		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);

		TypedQuery<ResumoLancamento> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));

	}

	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder,
			Root<Lancamento> root) {
		List<Predicate> predicates = new ArrayList<>();

//		sem metamodel: builder.like( builder.lower(root.get("descricao")), "%" + lancamentoFilter.getDescricao().toLowerCase() + "%"
//		 metamodel analisa a classe e cria clones das entidades de forma q quando eu atualizo as entidades ele atualiza os metamodels
//		eh util pra vc n precisar strings dentro dos parametros das consulta tipo root.get("descricao")

//		projeto -> propriedades -> java compiler -> anotation processing -> enable project specific settings
//		generated source directory: src/main/java
//		factory path -> add external jar
//		repository -> org -> hibernate -> jpa-modelgen -> 5.0.12.Final.jar

		/*
		 * dependency groupId org.hibernate artifactId hibernate-jpamodelgen
		 */
// 		vai gerar os metamodels dentro de model		

		if (!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
			predicates.add(builder.like(builder.lower(root.get(Lancamento_.descricao)),
					"%" + lancamentoFilter.getDescricao().toLowerCase() + "%"));
		}

		if (lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento),
					lancamentoFilter.getDataVencimentoDe()));
		}

		if (lancamentoFilter.getDataVencimentoAte() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento),
					lancamentoFilter.getDataVencimentoAte()));
		}

		return predicates.toArray(new Predicate[predicates.size()]);
	}

	private void adicionarRestricoesDePaginacao(TypedQuery<?> query, Pageable pageable) {
		// TODO Auto-generated method stub
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;

		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
	}

	private Long total(LancamentoFilter lancamentoFilter) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);

		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);

		criteria.select(builder.count(root));
		return manager.createQuery(criteria).getSingleResult();
	}

	@Override
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();

		CriteriaQuery<LancamentoEstatisticaCategoria> criteria = builder
				.createQuery(LancamentoEstatisticaCategoria.class);

		Root<Lancamento> root = criteria.from(Lancamento.class);

		criteria.select(builder.construct(LancamentoEstatisticaCategoria.class, root.get(Lancamento_.categoria),
				builder.sum(root.get(Lancamento_.valor))));

		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());

		criteria.where(builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), primeiroDia),
				builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), ultimoDia)

		);

		criteria.groupBy(root.get(Lancamento_.categoria));
		TypedQuery<LancamentoEstatisticaCategoria> typedQuery = manager.createQuery(criteria);
		return typedQuery.getResultList();
	}

	@Override
	public List<LancamentoEstatisticaDia> porDia(LocalDate mesReferencia) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();

		CriteriaQuery<LancamentoEstatisticaDia> criteria = builder.createQuery(LancamentoEstatisticaDia.class);

		Root<Lancamento> root = criteria.from(Lancamento.class);

		criteria.select(builder.construct(LancamentoEstatisticaDia.class, root.get(Lancamento_.tipo),
				root.get(Lancamento_.dataVencimento), builder.sum(root.get(Lancamento_.valor))));

		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());

		criteria.where(builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), primeiroDia),
				builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), ultimoDia)

		);

		criteria.groupBy(root.get(Lancamento_.tipo), root.get(Lancamento_.dataVencimento));
		TypedQuery<LancamentoEstatisticaDia> typedQuery = manager.createQuery(criteria);
		return typedQuery.getResultList();
	}

	@Override
	public List<LancamentoEstatisticaPessoa> porPessoa(LocalDate inicio, LocalDate fim) {
		CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();

		CriteriaQuery<LancamentoEstatisticaPessoa> criteriaQuery = criteriaBuilder
				.createQuery(LancamentoEstatisticaPessoa.class);

		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);

		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaPessoa.class, root.get(Lancamento_.tipo),
				root.get(Lancamento_.pessoa), criteriaBuilder.sum(root.get(Lancamento_.valor))));

		criteriaQuery.where(criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), inicio),
				criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), fim)

		);

		criteriaQuery.groupBy(root.get(Lancamento_.tipo), root.get(Lancamento_.pessoa));
		TypedQuery<LancamentoEstatisticaPessoa> typedQuery = manager.createQuery(criteriaQuery);
		return typedQuery.getResultList();
	}

}