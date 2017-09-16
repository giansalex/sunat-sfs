package sfs.service;

import static sfs.util.Constantes.CONSTANTE_FORMATO;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;




import javax.annotation.PostConstruct;
//import javax.annotation.PostConstruct;
import javax.xml.ws.Endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sfs.ws.TransferirArchivoImpl;

@Service
public class ConfigureServerServiceImpl implements ConfigureServerService{
	
	@Autowired
	private ComunesService comunesService;
	
	@Autowired
	private TransferirArchivoImpl transferirArchivoImpl;
	
	private static final Log log = LogFactory.getLog(ConfigureServerServiceImpl.class);
	
	@PostConstruct
	@Override
	public void publicarServicio(){
		String nombreArchivo="constantes.properties";
		Properties prop = new Properties();
		InputStream input = null;
		try{
			String rutaArchivoProperties = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO) + nombreArchivo;
			input = new FileInputStream(rutaArchivoProperties);
			prop.load(input);
			String bindingURI = prop.getProperty("RUTA_WS_EPT")!=null?prop.getProperty("RUTA_WS_EPT"):"XX";
			input.close();
	        Endpoint.publish(bindingURI, transferirArchivoImpl);
		}catch(Exception e){
			log.error("Error: " + e.getMessage() + " Causa:" + e.getCause() );
			
		}
	}
	

}
