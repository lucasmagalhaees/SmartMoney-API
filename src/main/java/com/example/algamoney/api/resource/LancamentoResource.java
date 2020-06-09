
package com.example.algamoney.api.resource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.dto.LancamentoEstatisticaCategoria;
import com.example.algamoney.api.dto.LancamentoEstatisticaDia;
import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.exceptionhandler.CustomResponseException;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.repository.LancamentosRepository;
import com.example.algamoney.api.repository.filter.LancamentoFilter;
import com.example.algamoney.api.repository.projection.ResumoLancamento;
import com.example.algamoney.api.service.LancamentoService;
import com.google.common.net.HttpHeaders;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/lancamentos")
@Api(tags = "CRUD de Lançamentos")
public class LancamentoResource {
	
	
	@Autowired
	private LancamentosRepository lancamentoRepository;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@Autowired
	private LancamentoService lancamentoService;
	
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and #oauth2.hasScope('read')")
	@GetMapping
	@ApiOperation("Endpoint para listar os lançamentos cadastrados")
	public Page<Lancamento> pesquisar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		return lancamentoRepository.filtrar(lancamentoFilter, pageable);
	}
	
	@GetMapping("/estatistica/categoria/{mes}")
	public List<LancamentoEstatisticaCategoria> porCategoria(@PathVariable String mes){
		return this.lancamentoRepository.porCategoria(LocalDate.parse(mes));
	}
	@GetMapping("/estatistica/tipo/{mes}")
	public List<LancamentoEstatisticaDia> porDia(@PathVariable String mes){
		return this.lancamentoRepository.porDia(LocalDate.parse(mes));
	}
	
	@GetMapping("/relatorio/pessoa/{inicio}/{fim}")
	public ResponseEntity<?> relatorioPorPessoa(@PathVariable String inicio, @PathVariable String fim) throws Exception{
		byte[] relatorio = lancamentoService.relatorioPorPessoa(LocalDate.parse(inicio), LocalDate.parse(fim));
		return ResponseEntity.ok().
				header(HttpHeaders.CONTENT_TYPE, org.springframework.http.MediaType.APPLICATION_PDF_VALUE).body(relatorio);
	}
	@GetMapping("/relatorio/avancado/{inicio}/{fim}")
	public ResponseEntity<?> relatorioPorPessoaAvancado(@PathVariable String inicio, @PathVariable String fim) throws Exception{
		byte[] relatorio = lancamentoService.relatorioPorPessoaAvancado(inicio, fim);
		return ResponseEntity.ok().
				header(HttpHeaders.CONTENT_TYPE, org.springframework.http.MediaType.APPLICATION_PDF_VALUE).body(relatorio);
	}
	
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and #oauth2.hasScope('read')")
	@GetMapping("/projecao")
	@ApiOperation("Endpoint para listar uma projeção dos lançamentos cadastrados")
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		return lancamentoRepository.resumir(lancamentoFilter, pageable);
	}
	
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and #oauth2.hasScope('read')")
	@GetMapping("/{codigo}")
	@ApiOperation("Endpoint para buscar um lançamento pelo código")
	public ResponseEntity<Lancamento> buscarPeloCodigo(@PathVariable Long codigo) {
		Lancamento lancamento = lancamentoRepository.findOne(codigo);
		return lancamento != null ? ResponseEntity.ok(lancamento) : ResponseEntity.notFound().build();
	}
	
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_LANCAMENTO') and #oauth2.hasScope('write')")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Endpoint para cadastrar um lançamento")
	public ResponseEntity<Lancamento> criar(@Valid @RequestBody Lancamento lancamento, HttpServletResponse response) {
	
	
	Optional <Lancamento> lancamentoExistente = lancamentoRepository.findByDescricao(lancamento.getDescricao());
	
	if (lancamentoExistente.isPresent()) {
		throw new CustomResponseException("Já existe um lancamento com esse nome", HttpStatus.BAD_REQUEST);
	} else {
		Lancamento lancamentoSalvo = lancamentoService.salvar(lancamento);
		  lancamentoSalvo = lancamentoRepository.findOne(lancamentoSalvo.getCodigo());
		publisher.publishEvent(new RecursoCriadoEvent(this, response, lancamentoSalvo.getCodigo()));
		return ResponseEntity.status(HttpStatus.CREATED).body(lancamentoSalvo);
		
	}
	}
	
	@PutMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_LANCAMENTO')")
	@ApiOperation("Endpoint para atualizar um lançamento")
	public ResponseEntity<Lancamento> atualizar(@PathVariable Long codigo, @Valid @RequestBody Lancamento lancamento) {
		try {
			Lancamento lancamentoSalvo = lancamentoService.atualizar(codigo, lancamento);
			return ResponseEntity.ok(lancamentoSalvo);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}
	
		
	
	
	@PreAuthorize("hasAuthority('ROLE_REMOVER_LANCAMENTO') and #oauth2.hasScope('write')")
	@DeleteMapping("/{codigo}")
	@ApiOperation("Endpoint para excluir um lançamento")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable Long codigo) {
		lancamentoRepository.delete(codigo);
	}
	

	
}