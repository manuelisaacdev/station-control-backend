package com.stationcontrol.service.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.stationcontrol.dto.SenhaDTO;
import com.stationcontrol.dto.UpdateEmailDTO;
import com.stationcontrol.exception.DataNotFoundException;
import com.stationcontrol.exception.UnauthorizedException;
import com.stationcontrol.model.Funcionario;
import com.stationcontrol.model.Pais;
import com.stationcontrol.model.Telefone;
import com.stationcontrol.repository.FuncionarioRepository;
import com.stationcontrol.repository.TelefoneRepository;
import com.stationcontrol.service.AbstractService;
import com.stationcontrol.service.FuncionarioService;
import com.stationcontrol.storage.StorageService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class FuncionarioServiceImpl extends AbstractServiceImpl<Funcionario, UUID, FuncionarioRepository> implements FuncionarioService {
	
	private final PasswordEncoder encoder;

	private final StorageService storageService;

	private final TelefoneRepository telefoneRepository;
	
	private final AbstractService<Pais, UUID> paisService;
	

	public FuncionarioServiceImpl(FuncionarioRepository repository, HttpServletRequest request,
			MessageSource messageSource, PasswordEncoder encoder, StorageService storageService,
			TelefoneRepository telefoneRepository, AbstractService<Pais, UUID> paisService) {
		super(repository, request, messageSource);
		this.encoder = encoder;
		this.storageService = storageService;
		this.telefoneRepository = telefoneRepository;
		this.paisService = paisService;
	}

	@Override
	public Funcionario findByEmail(String email) throws DataNotFoundException {
		return this.repository.findByEmail(email)
		.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.notfound", null, request.getLocale())));
	}
	
	@Override
	public Funcionario create(UUID idPais, Funcionario funcionario, Telefone telefone, Optional<MultipartFile> fotoPerfil) {
		telefone.setFuncionario(funcionario);
		funcionario.setTelefones(List.of(telefone));
		funcionario.setPais(paisService.findById(idPais));
		funcionario.setSenha(encoder.encode(funcionario.getSenha()));
		if (fotoPerfil.isPresent()) {
			funcionario.setFotoPerfil(storageService.store(fotoPerfil.get()));
		}
		try {			
			return super.save(funcionario);
		} catch (DataIntegrityViolationException e) {
			storageService.delete(funcionario.getFotoPerfil());
			throw new UnauthorizedException(messageSource.getMessage("employee.email.already-exists", new String[]{funcionario.getEmail()}, request.getLocale()));
		}
	}
	
	@Override
	public List<Funcionario> create(List<Funcionario> funcionarios) {
		var paises = paisService.findAll();
		SecureRandom random = new SecureRandom();
		List<Telefone> telefones = new ArrayList<>(funcionarios.size());
		var entitys = super.save(funcionarios.stream().map(funcionario -> {
			funcionario.setSenha(encoder.encode(funcionario.getSenha()));
			funcionario.setPais(paises.get(random.nextInt(paises.size() - 1)));
			return funcionario;
		}).toList());
		for (Funcionario funcionario : entitys) {
			telefones.addAll(funcionario.getTelefones());
		}
		telefoneRepository.saveAll(telefones);
		return entitys;
	}
	
	@Override
	public Funcionario update(UUID idFuncionario, Funcionario funcionario) {
		var entity = this.findById(idFuncionario);
		entity.setNome(funcionario.getNome());
		entity.setGenero(funcionario.getGenero());
		entity.setDataNascimento(funcionario.getDataNascimento());
		entity.setMorada(funcionario.getMorada());
		entity.setNotaInformativa(funcionario.getNotaInformativa());
		return super.save(entity);
	}
	
	@Override
	public Funcionario updateEmail(UUID idFuncionario, UpdateEmailDTO updateEmailDTO) throws UnauthorizedException {
		var entity = this.findById(idFuncionario);
		if (!encoder.matches(updateEmailDTO.getSenha(), entity.getSenha())) {
			throw new UnauthorizedException(messageSource.getMessage("unauthorized", null, request.getLocale()));
		}
		entity.setEmail(updateEmailDTO.getEmail());
		return super.save(entity);
	}
	
	@Override
	public Funcionario updateCountry(UUID idFuncionario, UUID idPais) {
		var entity = this.findById(idFuncionario);
		entity.setPais(paisService.findById(idPais));
		return super.save(entity);
	}
	
	@Override
	public Funcionario updatePassword(UUID idFuncionario, SenhaDTO senhaDTO) throws UnauthorizedException {
		var entity = this.findById(idFuncionario);
		if (!encoder.matches(senhaDTO.getAntiga(), entity.getSenha())) {
			throw new UnauthorizedException(messageSource.getMessage("employee.password.invalid", null, request.getLocale()));
		}
		entity.setSenha(encoder.encode(senhaDTO.getNova()));
		return super.save(entity);
	}
	
	@Override
	public Funcionario updateProfilePhoto(UUID idFuncionario, MultipartFile fotoPerfil) {
		var entity = this.findById(idFuncionario);
		String filename = storageService.store(fotoPerfil);
		storageService.delete(entity.getFotoPerfil());
		entity.setFotoPerfil(filename);
		return super.save(entity);
	}
	
	@Override
	public Funcionario delete(UUID id) {
		Funcionario funcionario = super.delete(id);
		storageService.delete(funcionario.getFotoPerfil());
		return funcionario;	
	}
}
