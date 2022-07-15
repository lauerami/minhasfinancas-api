package com.dsousa.minhasfinancas.model.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dsousa.minhasfinancas.model.entity.Usuario;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UsuarioRepositoryTest {

	@Autowired
	UsuarioRepository repository;
	
	@Autowired
	TestEntityManager entityManager;
	
	@Test
	public void deveVerificarAExistenciaDeUmEmail() {
		//scenario
		Usuario usuario = criarUsuario();
		entityManager.persist(usuario);
		
		//action / execution
		boolean result = repository.existsByEmail("usuario@email.com");
		
		//verification
		Assertions.assertThat(result).isTrue();
	}
	
	@Test
	public void deveRetornarFalsoQuandoNaoHouverUsuarioCadastradoComOEmail() {
		//action / execution
		boolean result = repository.existsByEmail("usuario@email.com");
		
		//verification
		Assertions.assertThat(result).isFalse();
	}
	
	@Test
	public void devePersistirUmUsuarioNaBaseDeDados() {
		//scenario
		Usuario usuario = criarUsuario();
		
		//action
		Usuario usuarioSalvo = repository.save(usuario);
		
		//verification
		Assertions.assertThat(usuarioSalvo.getId()).isNotNull();
	}
	
	@Test
	public void deveBuscarUmUsuarioPorEmail() {
		//scenario
		Usuario usuario = criarUsuario();
		entityManager.persist(usuario);
		
		//action
		Optional<Usuario> result = repository.findByEmail("usuario@email.com");
	
		//verification
		Assertions.assertThat(result.isPresent()).isTrue();
	}
	
	@Test
	public void deveRetornarVazioAoBuscarUsuarioPorEmailQuandoNaoExisteNaBase() {
		//action
		Optional<Usuario> result = repository.findByEmail("usuario@email.com");
	
		//verification
		Assertions.assertThat(result.isPresent()).isFalse();
	}
	
	public static Usuario criarUsuario() {
		Usuario usuario = Usuario.builder()
						 .nome("usuario")
						 .email("usuario@email.com")
						 .senha("senha")
						 .build();
		
		return usuario;		 	
	}
	
	
	
}
