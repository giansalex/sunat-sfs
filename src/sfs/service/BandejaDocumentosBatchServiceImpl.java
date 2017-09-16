package sfs.service;

import static sfs.util.Constantes.CONSTANTE_FORMATO;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BandejaDocumentosBatchServiceImpl implements BandejaDocumentosBatchService {
	
	@Autowired
	private GenerarDocumentosService generarDocumentosService;
	
	@Autowired
	private ComunesService comunesService;
	
	@Override
	public Map<String,String> actualizarEstadoBaja(String rutaArchivo, String nroTicket) throws Exception{
		String nombreArchivo="constantes.properties";
				
		Properties prop = new Properties();
		InputStream input = null;
		String rutaArchivoProperties = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO) + nombreArchivo;
		input = new FileInputStream(rutaArchivoProperties);
		prop.load(input);
		String urlWebService = prop.getProperty("RUTA_SERV_CDP")!=null?prop.getProperty("RUTA_SERV_CDP"):"XX";
		input.close();
				
		Map<String,String> resultado = generarDocumentosService.obtenerEstadoTicket(rutaArchivo, urlWebService, nroTicket);
		return resultado;
		
	}

}
