package com.dsousa.minhasfinancas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.h2.api.DatabaseEventListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dsousa.minhasfinancas.exception.RegraNegocioException;
import com.dsousa.minhasfinancas.model.entity.Lancamento;
import com.dsousa.minhasfinancas.model.entity.Usuario;
import com.dsousa.minhasfinancas.model.enums.StatusLancamento;
import com.dsousa.minhasfinancas.model.repository.LancamentoRepository;
import com.dsousa.minhasfinancas.model.repository.LancamentoRepositoryTest;
import com.dsousa.minhasfinancas.service.impl.LancamentoServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

	@SpyBean
	LancamentoServiceImpl service;
	
	@MockBean
	LancamentoRepository repository;
	
	@Test
	public void deveSalvarUmLancamento() {
		//scenario
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doNothing().when(service).validar(lancamentoASalvar);
		
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);
		
		//action
		Lancamento lancamento = service.salvar(lancamentoASalvar);
		
		//verification
		Assertions.assertEquals(lancamento.getId(), lancamentoSalvo.getId());
		Assertions.assertEquals(lancamento.getStatus(), StatusLancamento.PENDENTE);
	}
	
	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
		//Scenario
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);
		
		//action and Verification
		Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.salvar(lancamentoASalvar); }
        );
		
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
	}
	
	@Test
	public void deveAtualizarUmLancamento() {
		//scenario
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		
		Mockito.doNothing().when(service).validar(lancamentoSalvo);
		Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);
		
		//action
		service.atualizar(lancamentoSalvo);
		
		//verification
		Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);
		
	}
	
	@Test
	public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
		//Scenario
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();

		//action and Verification
		Assertions.assertThrows(
				NullPointerException.class,
                () -> { service.atualizar(lancamentoASalvar); }
        );
		
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
	}
	
	
	@Test
	public void deveDeletarUmLancamento() {
		//Scenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		
		//action
		service.deletar(lancamento);
		
		//verification
		Mockito.verify(repository).delete(lancamento);
	}
	
	@Test
	public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo() {
		//Scenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		
		//action
		Assertions.assertThrows(
				NullPointerException.class,
                () -> { service.deletar(lancamento); }
        );

		//verification
		Mockito.verify(repository, Mockito.never()).save(lancamento);
	}
	
	@Test
	public void deveFiltrarLancamentos() {
		//scenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		
		List<Lancamento> lista = Arrays.asList(lancamento);
		Mockito.when(repository.findAll(Mockito.any(Example.class))).thenReturn(lista);
				
		//Action
		List<Lancamento> resultado = service.buscar(lancamento);
		
		//verification
		Assertions.assertAll("Verifica o resultado da busca.",
			    () -> Assertions.assertFalse(resultado.isEmpty()),
			    () -> Assertions.assertEquals(resultado.size(), 1),
			    () -> Assertions.assertIterableEquals(lista, resultado)
			);
	}
	
	@Test
	public void deveAtualizarOStatusDeUmLancamento() {
		//scenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		
		StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
		Mockito.doReturn(lancamento).when(service).atualizar(lancamento);
		
		//action
		service.atualizarStatus(lancamento, novoStatus);
		
		//verification
		Assertions.assertEquals(lancamento.getStatus(), novoStatus);
		Mockito.verify(service).atualizar(lancamento);
	}
	
	@Test
	public void deveObterUmLancamentoPorID() {
		//scenario
		Long id = 1l;
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));
		
		//action
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		//verification
		Assertions.assertTrue(resultado.isPresent());
		
	}
	
	@Test
	public void deveRetornarVazioQuandoOLancamentoNaoExiste() {
		//scenario
		Long id = 1l;
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		//action
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		//verification
		Assertions.assertFalse(resultado.isPresent());
		
	}
	
	@Test
	public void deveLancarErrosAoValidarUmLancamento() {
		//scenario
		Lancamento lancamento = new Lancamento();
		
		//action
		RegraNegocioException thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		String expected = "Informe uma descrição válida.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		lancamento.setDescricao("");
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe uma descrição válida.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		lancamento.setDescricao("Salario.");
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe um mês válido.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		lancamento.setMes(0);
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe um mês válido.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		lancamento.setMes(13);
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe um mês válido.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		lancamento.setMes(2);
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe um Ano válido.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		lancamento.setAno(203);
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe um Ano válido.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		lancamento.setAno(2022);
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe um Usuário.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		String email = "email@email.com";
		String senha = "senha";
		Usuario usuario = Usuario.builder().email(email).senha(senha).build();
		lancamento.setUsuario(usuario);
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe um Usuário.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		email = "email@email.com";
		senha = "senha";
		usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		lancamento.setUsuario(usuario);
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe um valor válido.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		lancamento.setValor(BigDecimal.ZERO);
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe um valor válido.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
		lancamento.setValor(BigDecimal.TEN);
		
		//action
		thrown = Assertions.assertThrows(
				RegraNegocioException.class,
                () -> { service.validar(lancamento); }
        );
		
		//verification
		expected = "Informe um tipo de lançamento.";
		Assertions.assertEquals(expected, thrown.getMessage());
		
	}
	
	
}
