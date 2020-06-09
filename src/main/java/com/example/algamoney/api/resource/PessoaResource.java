package com.example.algamoney.api.resource;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.exceptionhandler.CustomResponseException;
import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.repository.PessoasRepository;
import com.example.algamoney.api.repository.filter.PessoaFilter;
import com.example.algamoney.api.service.PessoaService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/pessoas")
@Api(tags = "CRUD de Pessoas")
public class PessoaResource {
	
	@Autowired
	private PessoasRepository pessoaRepository;
	
	
	@Autowired
	private PessoaService pessoaService;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	
//	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_PESSOA') and #oauth2.hasScope('read')")
//	@GetMapping
//	public List<Pessoa> listar(){
//		return pessoaRepository.findAll();
//	}
	
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and #oauth2.hasScope('read')")
	@GetMapping
	@ApiOperation("Endpoint para listar todas as pessoas cadastradas")
	public Page<Pessoa> listar(PessoaFilter pessoaFilter, Pageable pageable) {
		return pessoaRepository.filtrar(pessoaFilter, pageable);
	}
	
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_PESSOA') and #oauth2.hasScope('read')")
	@GetMapping("/{codigo}")
	@ApiOperation("Endpoint para buscar uma pessoa por código")
	public ResponseEntity<Pessoa> buscarPeloCodigo(@PathVariable Long codigo) {
		Pessoa pessoa = pessoaRepository.findOne(codigo);

		return pessoa == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(pessoa);
	}
	
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_PESSOA') and #oauth2.hasScope('write')")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Endpoint para cadastrar uma pessoa")
	public ResponseEntity<Pessoa> criar(@Valid @RequestBody Pessoa pessoa, HttpServletResponse response) {
	
		Optional <Pessoa> pessoaExistente = pessoaRepository.findByNome(pessoa.getNome());
		
		if (pessoaExistente.isPresent()) {
			throw new CustomResponseException("Já existe uma pessoa com este nome.", HttpStatus.BAD_REQUEST);
		} else {
			Pessoa pessoaSalva = pessoaRepository.save(pessoa);
			publisher.publishEvent(new RecursoCriadoEvent(this, response, pessoaSalva.getCodigo()));
			return ResponseEntity.status(HttpStatus.CREATED).body(pessoaSalva);
			
		}
		
	}	

	@PreAuthorize("hasAuthority('ROLE_REMOVER_PESSOA') and #oauth2.hasScope('write')")
	@DeleteMapping("/{codigo}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation("Endpoint para deletar uma pessoa")
	public void remover(@PathVariable Long codigo) {
		pessoaRepository.delete(codigo);
	}
	
	
	@PutMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_PESSOA') and #oauth2.hasScope('write')")
	@ApiOperation("Endpoint para editar uma pessoa")
	public ResponseEntity<Pessoa> atualizar(@PathVariable Long codigo, @Valid @RequestBody Pessoa pessoa){
		Pessoa pessoaSalva = pessoaService.atualizar(codigo, pessoa);
		return ResponseEntity.ok(pessoaSalva);
		
		
	}
	
	@PutMapping("/{codigo}/ativo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_PESSOA') and #oauth2.hasScope('write')")
	@ApiOperation("Endpoint para ativar ou inativar uma pessoa")
	public void atualizarPropriedadeAtivo(@PathVariable Long codigo, @RequestBody Boolean ativo) {
		pessoaService.atualizarPropriedadeAtivo(codigo, ativo);
	}
	

}
