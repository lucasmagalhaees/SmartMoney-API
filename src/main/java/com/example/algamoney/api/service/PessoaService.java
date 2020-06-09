package com.example.algamoney.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.example.algamoney.api.dto.LancamentoEstatisticaPessoa;
import com.example.algamoney.api.dto.LancamentoEstatisticaPessoaDTO;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.model.TipoLancamento;
import com.example.algamoney.api.repository.LancamentosRepository;
import com.example.algamoney.api.repository.PessoasRepository;

@Service
public class PessoaService {

	@Autowired
	private PessoasRepository pessoaRepository;

	@Autowired
	private LancamentosRepository lancamentoRepository;

	public Pessoa atualizar(Long codigo, Pessoa pessoa) {
		Pessoa pessoaSalva = buscarPessoaPeloCodigo(codigo);
		BeanUtils.copyProperties(pessoa, pessoaSalva, "codigo");
		return pessoaRepository.save(pessoaSalva);
	}

	public void atualizarPropriedadeAtivo(Long codigo, Boolean ativo) {
		Pessoa pessoaSalva = buscarPessoaPeloCodigo(codigo);
		pessoaSalva.setAtivo(ativo);
		pessoaRepository.save(pessoaSalva);

	}

	public Pessoa buscarPessoaPeloCodigo(Long codigo) {
		Pessoa pessoaSalva = pessoaRepository.findOne(codigo);
		if (pessoaSalva == null) {
			throw new EmptyResultDataAccessException(1);

		}
		return pessoaSalva;
	}

	public LancamentoEstatisticaPessoaDTO preencherLancamentoEstatisticaPessoa(Pessoa pessoa) {
		LancamentoEstatisticaPessoaDTO dto = new LancamentoEstatisticaPessoaDTO();
		dto.setPessoa(pessoa);
		List<Lancamento> listaLancamentos = this.lancamentoRepository.findByPessoa(pessoa);
		BigDecimal despesa = listaLancamentos.stream().filter(a -> a.getTipo().equals(TipoLancamento.DESPESA))
				.map(b -> b.getValor()).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal receita = listaLancamentos.stream().filter(a -> a.getTipo().equals(TipoLancamento.RECEITA))
				.map(b -> b.getValor()).reduce(BigDecimal.ZERO, BigDecimal::add);
		if (receita.compareTo(despesa) > 0) {
			dto.setTipo(TipoLancamento.RECEITA.getDescricao());
			dto.setTotal(formataParaReal(receita.subtract(despesa).setScale(2, RoundingMode.HALF_UP)));
		} else if (receita.compareTo(despesa) < 0) {
			dto.setTipo(TipoLancamento.DESPESA.getDescricao());
			dto.setTotal(formataParaReal(despesa.subtract(receita).setScale(2, RoundingMode.HALF_UP)));
		} else {
			dto.setTipo("Saldo nulo");
			dto.setTotal(formataParaReal(BigDecimal.ZERO.setScale(2)));
		}

		return dto;
	}

	public List<LancamentoEstatisticaPessoaDTO> buscarPessoasRelatorioLancamento(String inicio, String fim) {
		
		System.out.println(inicio);
		System.out.println(fim);
		
		List<Pessoa> listaPessoas = this.pessoaRepository.buscarPessoasRelatorioLancamento(LocalDate.parse(inicio),
				LocalDate.parse(fim));

		return listaPessoas.stream().map(pessoa -> preencherLancamentoEstatisticaPessoa(pessoa))
				.collect(Collectors.toList());
	}

	public static String formataParaReal(BigDecimal valor) {
		return "R$ " + valor;
	}
}
