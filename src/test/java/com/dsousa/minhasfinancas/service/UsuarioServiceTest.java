package com.dsousa.minhasfinancas.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dsousa.minhasfinancas.exception.ErroAutenticacao;
import com.dsousa.minhasfinancas.exception.RegraNegocioException;
import com.dsousa.minhasfinancas.model.entity.Usuario;
import com.dsousa.minhasfinancas.model.repository.UsuarioRepository;
import com.dsousa.minhasfinancas.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

	@SpyBean
	UsuarioServiceImpl service;
	
	@MockBean
	UsuarioRepository repository;
	
	
	@Test
	public void deveValidarEmail() {
		// scenario
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
		
		// action and verification
		Assertions.assertDoesNotThrow(() -> {service.validarEmail("email@email.com");});
	}
	
	@Test
	public void deveLancarErroAoValidarQuandoExistirEmailCadastrado() {
		//scenario
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
		
		// action and verification
		RegraNegocioException thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validarEmail("email@email.com"); }
        );
		String expected = "Já existe um usuário cadastrado com este email.";
		Assertions.assertEquals(expected, thrown.getMessage());
	}
	
	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
		//scenario
		String email = "email@email.com";
		String senha = "senha";
		
		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
		
		//action
		Usuario result = service.autenticar(email, senha);
		
		//verification
		Assertions.assertNotNull(result);
	}
	
	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {
		//scenario
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
		
		//action and Verification
		ErroAutenticacao thrown = Assertions.assertThrows(
				ErroAutenticacao.class,
                () -> { service.autenticar("usuario@email.com", "senha"); }
        );
		String expected = "Usuário não encontrado para o email informado";
		Assertions.assertEquals(expected, thrown.getMessage());
	}
	
	@Test
	public void deveLancarErroQuandoSenhaForInvalida() {
		//scenario
		String email = "email@email.com";
		String senha = "senha";
		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));
		
		//action and Verification
		ErroAutenticacao thrown = Assertions.assertThrows(
				ErroAutenticacao.class,
                () -> { service.autenticar("usuario@email.com", "123"); }
        );
		String expected = "Senha inválida";
		Assertions.assertEquals(expected, thrown.getMessage());
	}
	
	@Test
	public void deveSalvarUmUsuario() {
		//scenario
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		Usuario usuario = Usuario.builder()
				 .nome("nome")
				 .email("usuario@email.com")
				 .senha("senha")
				 .id(1l)
				 .build();
		
		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);
		
		//action
		Usuario usuarioSalvo = service.salvarUsuario(new Usuario());
		
		//verification
		Assertions.assertNotNull(usuarioSalvo);
		Assertions.assertEquals(usuarioSalvo.getId(), 1l);
		Assertions.assertEquals(usuarioSalvo.getNome(), "nome");
		Assertions.assertEquals(usuarioSalvo.getEmail(), "usuario@email.com");
		Assertions.assertEquals(usuarioSalvo.getSenha(), "senha");
		
	}
	
	@Test
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {
		//scenario
		String email = "usuario@email.com";
		Usuario usuario = Usuario.builder()
				 .email(email)
				 .build();
		
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);
		
		//action and Verification
		Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.salvarUsuario(usuario); }
        );
		
		Mockito.verify(repository, Mockito.never()).save(usuario);
		
	}
	
}
