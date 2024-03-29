package com.stationcontrol.service;

import java.util.List;
import java.util.UUID;

import com.stationcontrol.exception.BadRequestException;
import com.stationcontrol.model.NotificacaoRequerente;

public interface NotificacaoRequerenteService extends AbstractService<NotificacaoRequerente, UUID> {
	public NotificacaoRequerente create(UUID idFuncionario, UUID idRequerente, NotificacaoRequerente notificacaoRequerente);
	public List<NotificacaoRequerente> create(List<NotificacaoRequerente> notificacoesRequerentes);
	public NotificacaoRequerente update(UUID idNotificacao, NotificacaoRequerente notificacaoRequerente);
	public NotificacaoRequerente markReceived(UUID idNotificacao) throws BadRequestException;

}
