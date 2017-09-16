package sfs.service;

import java.util.Map;

public interface BandejaDocumentosBatchService {
	
	
	public Map<String,String> actualizarEstadoBaja(String rutaArchivo, String nroTicket) throws Exception;
			

}
