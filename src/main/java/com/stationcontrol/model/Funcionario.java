package com.stationcontrol.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.Length;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SourceType;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.stationcontrol.model.converter.GeneroConverter;
import com.stationcontrol.model.converter.PapelConverter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonClassDescription("funcionario")
@JsonRootName(value = "funcionario", namespace = "funcionarios")
@Table(
	name = "funcionarios",
	indexes = {
		@Index(name = "idx_funcionarios_nome", columnList = "nome"),
		@Index(name = "idx_funcionarios_email", columnList = "email"),
		@Index(name = "idx_funcionarios_nome_email", columnList = "nome, email"),
	},
	uniqueConstraints = @UniqueConstraint(name = "uk_funcionarios_email", columnNames = "email")
)
@JsonPropertyOrder({"id", "nome", "genero", "dataNascimento", "email", "morada", "papel", "totalRequerentes", "totalSuspeitos", "totalOcorrencias", "totalSuspeitosOcorrencias", "dataCriacao", "fotoPerfil", "pais", "biografia"})
public class Funcionario {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(nullable = false)
	private String nome;

	@Column(nullable = false)
	@Convert(converter = GeneroConverter.class)
	private Genero genero;
	
	@Column(nullable = false)
	private LocalDate dataNascimento;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String morada;
		
	@Column(nullable = false)
	@Convert(converter = PapelConverter.class)
	private Papel papel;

	@Column(nullable = false)
	@JsonProperty(access = Access.WRITE_ONLY)
	private String senha;

	@Column(name = "biografia", length = Length.LONG32, nullable = false)
	private String biografia;

	@CreationTimestamp(source = SourceType.DB)
	@Column(name = "data_criacao", nullable = false, updatable = false)
	private LocalDateTime dataCriacao;

	@Column(name = "foto_perfil")
	private String fotoPerfil;
	
	@Formula("(SELECT COUNT(r.funcionario_id) FROM funcionarios f LEFT JOIN requerentes r ON (r.funcionario_id=f.id) WHERE f.id=id GROUP BY f.id)")
	private Long totalRequerentes;
	
	@Formula("(SELECT COUNT(s.funcionario_id) FROM funcionarios f LEFT JOIN suspeitos s ON (s.funcionario_id=f.id) WHERE f.id=id GROUP BY f.id)")
	private Long totalSuspeitos;
	
	@Formula("(SELECT COUNT(o.funcionario_id) FROM funcionarios f LEFT JOIN ocorrencias o ON (o.funcionario_id=f.id) WHERE f.id=id GROUP BY f.id)")
	private Long totalOcorrencias;
	
	@Formula("(SELECT COUNT(so.funcionario_id) FROM funcionarios f LEFT JOIN suspeitos_ocorrencias so ON (so.funcionario_id=f.id) WHERE f.id=id GROUP BY f.id)")
	private Long totalSuspeitosOcorrencias;
	
	@ManyToOne
	@JoinColumn(name = "pais_id", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_funcionarios_paises"))
	private Pais pais;

	@JoinTable(
		name="telefones_funcionarios",
		indexes = @Index(name = "idx_telefones_funcionarios_funcionario_id", columnList = "funcionario_id"),
		uniqueConstraints = @UniqueConstraint(name = "uk_telefones_funcionarios_funcionario_id_telefone_id", columnNames = {"funcionario_id", "telefone_id"}),
        joinColumns=@JoinColumn(name="funcionario_id", referencedColumnName="id", nullable = false, foreignKey = @ForeignKey(name = "fk_telefones_funcionarios_funcionario")),
        inverseJoinColumns=@JoinColumn(name="telefone_id", referencedColumnName="id", nullable = false, foreignKey = @ForeignKey(name = "fk_telefones_funcionarios_telefone"))
	)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<Telefone> telefones;
	
	@JsonIgnore
	@OneToMany(mappedBy = "funcionario", orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Ocorrencia> ocorrencias;
	
	@JsonIgnore
	@OneToMany(mappedBy = "funcionario", orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Requerente> requerentes;
	
	@JsonIgnore
	@OneToMany(mappedBy = "funcionario", orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Suspeito> suspeitos;
}
