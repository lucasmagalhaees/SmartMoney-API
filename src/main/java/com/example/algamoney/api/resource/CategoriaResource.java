package com.example.algamoney.api.resource;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.exceptionhandler.CustomResponseException;
import com.example.algamoney.api.model.Categoria;
import com.example.algamoney.api.repository.CategoriasRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@RestController
@Api(tags = "CRUD de Categorias")
@RequestMapping("/categorias")
public class CategoriaResource {
	
	@Autowired
	private CategoriasRepository categoriaRepository;
	
//	@CrossOrigin(maxAge = 10, origins = { "http://localhost:8000" }, allowedHeaders = "HEADER1")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_CATEGORIA') and #oauth2.hasScope('read')")
	@GetMapping
	@ApiOperation("Endpoint para buscar todas as categorias")
	public List<Categoria> listar(){
		return categoriaRepository.findAll();
	}
	
	/*public ResponseEntity<List<Categoria>> listar() {
		List<Categoria> categoria = categoriaRepository.findAll();
		return !categoria.isEmpty() ? ResponseEntity.ok(categoria) : ResponseEntity.noContent().build();
	
	} */
	
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_CATEGORIA') and #oauth2.hasScope('read')")
	@GetMapping("/{codigo}")
	@ApiOperation("Endpoint para buscar uma categoria pelo código")
	public ResponseEntity<Categoria> buscar(@PathVariable Long codigo) {
		Categoria categoria = categoriaRepository.findOne(codigo);
		
		if (!((List<Categoria>) categoria).isEmpty())
			return ResponseEntity.ok(categoria);
		else
			return ResponseEntity.noContent().build();
}
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	
	@ApiOperation("Endpoint para cadastro de uma categoria")
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_CATEGORIA') and #oauth2.hasScope('write')")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Categoria> criar(@Valid @RequestBody Categoria categoria, HttpServletResponse response) {
	
	Optional<Categoria> categoriaExistente = categoriaRepository.findByNome(categoria.getNome());
	if (categoriaExistente.isPresent()) {
		throw new CustomResponseException("Já existe uma categoria com este nome.", HttpStatus.BAD_REQUEST);
			
	} else {
		Categoria categoriaSalva = categoriaRepository.save(categoria);
		
		publisher.publishEvent(new RecursoCriadoEvent(this, response, categoriaSalva.getCodigo()));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(categoriaSalva);
	}
	
	}
	
	
}
