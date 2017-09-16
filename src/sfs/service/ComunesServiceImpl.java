package sfs.service;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sfs.model.dao.TxxxyDAO;
import sfs.model.domain.TxxxyBean;
import static sfs.util.Constantes.CONSTANTE_TEMP;
import static sfs.util.Constantes.CONSTANTE_PARSE;
import static sfs.util.Constantes.CONSTANTE_DATA;
import static sfs.util.Constantes.CONSTANTE_CERT;
import static sfs.util.Constantes.CONSTANTE_ENVIO;
import static sfs.util.Constantes.CONSTANTE_RPTA;
import static sfs.util.Constantes.CONSTANTE_FIRMA;
import static sfs.util.Constantes.CONSTANTE_REPO;
import static sfs.util.Constantes.CONSTANTE_FORMATO;
import static sfs.util.Constantes.CONSTANTE_ORIDAT;
import static sfs.util.Constantes.CONSTANTE_ALMCERT;
import static sfs.util.Constantes.CONSTANTE_VERSION_SFS;
import static sfs.util.Constantes.CONSTANTE_ELEMENTO_LIBRERIAS;
import static sfs.util.Constantes.CONSTANTE_ELEMENTO_COMPONENTE;
import static sfs.util.Constantes.CONSTANTE_ELEMENTO_FUENTES;
import static sfs.util.Constantes.CONSTANTE_URL_PRUEBA;

@Service
public class ComunesServiceImpl implements ComunesService {

	@Autowired
	private TxxxyDAO txxxyDAO;
			
	private static final Log log = LogFactory.getLog(ComunesServiceImpl.class);
	
	@Override
	public String obtenerRutaTrabajo(String rutaTrabajoBusqueda) throws Exception{
		log.debug("ComunesServiceImpl.obtenerRutaTrabajo...Inicio");
		StringBuilder rutaDirectorio = new StringBuilder();
		rutaDirectorio.setLength(0);
		
		TxxxyBean txxxyBean = new TxxxyBean();
		txxxyBean.setId_para("PARASIST");
		txxxyBean.setCod_para("RUTSOL");
		List<TxxxyBean> parametroRuta =  txxxyDAO.consultarParametro(txxxyBean);
		
		if (parametroRuta.size() > 0)
			rutaDirectorio.append(parametroRuta.get(0).getVal_para());
		else
			rutaDirectorio.append("");
		
		log.debug("ComunesServiceImpl.obtenerRutaTrabajo...Ruta Base: " + rutaDirectorio.toString());
				
		if(CONSTANTE_TEMP.equals(rutaTrabajoBusqueda))
			rutaDirectorio.append("/sunat_archivos/sfs/TEMP/");
		if(CONSTANTE_PARSE.equals(rutaTrabajoBusqueda))	
			rutaDirectorio.append( "/sunat_archivos/sfs/PARSE/");
		if(CONSTANTE_DATA.equals(rutaTrabajoBusqueda))
			rutaDirectorio.append("/sunat_archivos/sfs/DATA/");				
		if(CONSTANTE_CERT.equals(rutaTrabajoBusqueda))
			rutaDirectorio.append("/sunat_archivos/sfs/CERT/");					
		if(CONSTANTE_ENVIO.equals(rutaTrabajoBusqueda))
			rutaDirectorio.append("/sunat_archivos/sfs/ENVIO/");						
		if(CONSTANTE_RPTA.equals(rutaTrabajoBusqueda))
			rutaDirectorio.append("/sunat_archivos/sfs/RPTA/");			
		if(CONSTANTE_FIRMA.equals(rutaTrabajoBusqueda))
			rutaDirectorio.append("/sunat_archivos/sfs/FIRMA/");			
		if(CONSTANTE_REPO.equals(rutaTrabajoBusqueda))
			rutaDirectorio.append("/sunat_archivos/sfs/REPO/");			
		if(CONSTANTE_FORMATO.equals(rutaTrabajoBusqueda))
			rutaDirectorio.append("/sunat_archivos/sfs/VALI/");			
		if(CONSTANTE_ORIDAT.equals(rutaTrabajoBusqueda))
			rutaDirectorio.append("/sunat_archivos/sfs/ORIDAT/");			
		if(CONSTANTE_ALMCERT.equals(rutaTrabajoBusqueda))
			rutaDirectorio.append("/sunat_archivos/sfs/ALMCERT/");
		
		log.debug("ComunesServiceImpl.obtenerRutaTrabajo...Ruta Completa: " + rutaDirectorio.toString());
		
		File farchivo = new File(rutaDirectorio.toString());
		if(!farchivo.exists()||!farchivo.isDirectory())
			throw new Exception("No se encuentra la ruta para los archivos y directorios del facturador"); 
						
		log.debug("ComunesServiceImpl.obtenerRutaTrabajo...Final");
		return rutaDirectorio.toString();
		
	}
	
	@Override
	public void validarConexion(String direccion, Integer puerto) throws Exception{
		if (!tieneConexionInternet(direccion,puerto))
			throw new Exception("No tienen Acceso a Internet.");
	}

	@Override
	public Boolean validarVersionFacturador(String versionFacturador) throws Exception{
		log.debug("ComunesServiceImpl.validarVersionFacturador...Inicio");
		
		Boolean resultado = false;				
		String nombreArchivo="constantes.properties";
					
		Properties prop = new Properties();
		InputStream input = null;
		String rutaArchivoProperties = this.obtenerRutaTrabajo(CONSTANTE_FORMATO) + nombreArchivo;
		input = new FileInputStream(rutaArchivoProperties);
		prop.load(input);
		String origenHttp = prop.getProperty("RUTA_HTTP_UPD")!=null?prop.getProperty("RUTA_HTTP_UPD"):"XX";
		input.close();
		
		String rutaDestino = this.obtenerRutaTrabajo(CONSTANTE_FORMATO) + "sfsupdate.xml";
		
		Boolean conexionInternet = tieneConexionInternet(CONSTANTE_URL_PRUEBA,80);
		
		if(!conexionInternet)
			resultado = true;
		else{
			try	{
					log.debug("Copiando la URL a File");
					FileUtils.copyURLToFile(new URL(origenHttp), new File(rutaDestino));
					log.debug("Buscando Tag");
					String versionPublicada = this.leerEtiquetaArchivoXml(rutaDestino, "version");
					log.debug("Comparando valores");
					log.debug("Versión Publicada: " + versionPublicada);
					log.debug("Versión Facturador: " + CONSTANTE_VERSION_SFS);
					if(CONSTANTE_VERSION_SFS.equals(versionPublicada))
						resultado = true;
					else
						resultado = false;
					
					File archivoTemporal = new File(rutaDestino);
					if(archivoTemporal.exists())archivoTemporal.delete();
					
			}catch(Exception e){
				
					log.error("No se pudo validar la version del actualizador desde internet: " + e.getMessage());
			}
		}
		
		log.debug("ComunesServiceImpl.validarVersionFacturador...Final");
						
		return resultado;
	}
	
	@Override
	public Boolean actualizarVersionFacturador(String versionFacturador) throws Exception{
		Boolean resultado = true;				
		String nombreArchivo="constantes.properties";
		
		resultado = validarVersionFacturador(versionFacturador);
		
		if(!resultado){
				
			Properties prop = new Properties();
			InputStream input = null;
			String rutaArchivoProperties = this.obtenerRutaTrabajo(CONSTANTE_FORMATO) + nombreArchivo;
			input = new FileInputStream(rutaArchivoProperties);
			prop.load(input);
			String origenHttp = prop.getProperty("RUTA_HTTP_UPD")!=null?prop.getProperty("RUTA_HTTP_UPD"):"XX";
			input.close();
			
			String rutaDestino = this.obtenerRutaTrabajo(CONSTANTE_FORMATO) + "sfsupdate.xml";
			
			final String rutaBase = System.getenv("SUNAT_HOME");
			
			if((rutaBase == null)||"".equals(rutaBase))
				throw new Exception("No existe la varible de entorno SUNAT_HOME");	
			
			String rutaLibrerias = rutaBase + "/servers/sfs/lib/ext";
			String rutaFuentes =  rutaBase + "/servers/sfs/webapps";
			String rutaComponentes =  rutaBase + "/servers/sfs/webapps/a/js/swfacturador";
			
			
			try	{
					log.debug("Copiando la URL a File");
					FileUtils.copyURLToFile(new URL(origenHttp), new File(rutaDestino));
					log.debug("Buscando Tag de Librerias");
					List<String> librerias = leerEtiquetaListaArchivoXml(rutaDestino,"filesLibraries");
					for(String ruta : librerias){
						String[] tokens = ruta.split("\\/");
						Integer tamano = tokens.length; 
						if( tamano > 0){
							String fileName = tokens[tamano - 1];	
							log.debug("Copiando Archivo Librerias: " + fileName);						
							FileUtils.copyURLToFile(new URL(ruta), new File(rutaLibrerias,fileName));
						}
					}
					
					log.debug("Buscando Tag de Componentes Web");
					List<String> componentes = leerEtiquetaListaArchivoXml(rutaDestino,"filesWebComponents");
					for(String ruta : componentes){
						String[] tokens = ruta.split("\\/");
						Integer tamano = tokens.length; 
						if( tamano > 0){
							String fileName = tokens[tamano - 1];	
							log.debug("Copiando Archivo Componentes: " + fileName);						
							FileUtils.copyURLToFile(new URL(ruta), new File(rutaComponentes,fileName));
						}
					}
					
					log.debug("Buscando Tag de Fuentes");
					List<String> fuentes = leerEtiquetaListaArchivoXml(rutaDestino,"filesSources");
					for(String ruta : fuentes){
						String[] tokens = ruta.split("\\/");
						Integer tamano = tokens.length; 
						if( tamano > 0){
							String fileName = tokens[tamano - 1];	
							log.debug("Copiando Archivo Fuentes: " + fileName);
							FileUtils.copyURLToFile(new URL(ruta), new File(rutaFuentes,fileName));
						}
					}
					
			}catch(Exception e){
				
					log.error("No se pudo descargar la version actualizada desde internet: " + e.getMessage());
			}
		}
						
		return resultado;
	}
	
	
	@Override
	public String leerEtiquetaArchivoXml(String rutaArchivo, String nombreEtiqueta) throws Exception{
		
		File file = new File(rutaArchivo);
	    InputStream inputStream= new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream,"ISO8859_1");
	    
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    
	    Document document = dBuilder.parse(new InputSource(reader));
	    
        document.getDocumentElement().normalize();
        log.debug("Elemento Raíz :" + document.getDocumentElement().getNodeName());
        
        log.debug("Elemento :" + document.getElementsByTagName(nombreEtiqueta));
        
        log.debug("Valor :" + document.getElementsByTagName(nombreEtiqueta).item(0).getTextContent());
	    String informacion = document.getElementsByTagName(nombreEtiqueta).item(0).getTextContent();
		
	    inputStream.close();
	    
		return informacion;
		
	}
	
	@Override
	public List<String> leerEtiquetaListaArchivoXml(String rutaArchivo, String nombreEtiqueta) throws Exception{
		File file = new File(rutaArchivo);
	    InputStream inputStream= new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream,"ISO8859_1");
	    
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    
	    Document document = dBuilder.parse(new InputSource(reader));
	    
	    String contenido = "", elementoSub="";
	    List<String> listaRutas = new ArrayList<String>();
	    
        
        document.getDocumentElement().normalize();
        log.debug("Elemento Raíz :" + document.getDocumentElement().getNodeName());
        
        NodeList nList = document.getElementsByTagName(nombreEtiqueta);
        
        if(CONSTANTE_ELEMENTO_FUENTES.equals(nombreEtiqueta))
        	elementoSub = "fileSource";
        if(CONSTANTE_ELEMENTO_COMPONENTE.equals(nombreEtiqueta))
        	elementoSub = "fileWebComponent"; 
        if(CONSTANTE_ELEMENTO_LIBRERIAS.equals(nombreEtiqueta))
        	elementoSub = "fileLibrary"; 
        
        for (int i = 0; i < nList.getLength(); i++) {
    		Node nNode = nList.item(i);
    		log.debug("Elemento Actual :" + nNode.getNodeName());
    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    			Element eElement = (Element) nNode;
    			contenido = eElement.getElementsByTagName(elementoSub).item(0).getTextContent();
    			listaRutas.add(contenido);
    		}
        }
        	    
        inputStream.close();
		return listaRutas;
	}
	
	private Boolean tieneConexionInternet(String direccion, Integer puerto){
		Boolean retorno = false;
		try{
			  Socket socket = new Socket(direccion, puerto);
			  if(socket.isConnected())socket.close();
			  retorno = true;
			}catch(Exception e){
				
				log.debug("Error en Conexion a Internet: " + e.getMessage());
				retorno = false;  
			}
		
		return retorno;
		
	}
	

}
