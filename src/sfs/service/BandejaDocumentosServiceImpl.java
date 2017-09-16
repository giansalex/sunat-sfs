package sfs.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sfs.model.dao.TxxxxDAO;
import sfs.model.dao.TxxxyDAO;
import sfs.model.domain.TxxxxBean;
import sfs.model.domain.TxxxyBean;
import sfs.service.BandejaDocumentosService;
import static sfs.util.Constantes.CONSTANTE_ALMCERT;
import static sfs.util.Constantes.CONSTANTE_CERT;
import static sfs.util.Constantes.CONSTANTE_DATA;
import static sfs.util.Constantes.CONSTANTE_TEMP;
import static sfs.util.Constantes.CONSTANTE_FORMATO;
import static sfs.util.Constantes.CONSTANTE_SITUACION_CON_ERRORES;
import static sfs.util.Constantes.CONSTANTE_SITUACION_ENVIADO_RECHAZADO;
import static sfs.util.Constantes.CONSTANTE_SITUACION_POR_GENERAR_XML;
import static sfs.util.Constantes.CONSTANTE_SITUACION_XML_GENERADO;
import static sfs.util.Constantes.CONSTANTE_SITUACION_XML_VALIDAR;
import static sfs.util.Constantes.CONSTANTE_SUFIJO_ARCHIVO_BAJA;
import static sfs.util.Constantes.CONSTANTE_SUFIJO_ARCHIVO_CABE;
import static sfs.util.Constantes.CONSTANTE_SUFIJO_ARCHIVO_DETA;
import static sfs.util.Constantes.CONSTANTE_SUFIJO_ARCHIVO_RELA;
import static sfs.util.Constantes.CONSTANTE_SUFIJO_ARCHIVO_ACAB;
import static sfs.util.Constantes.CONSTANTE_SUFIJO_ARCHIVO_ADET;
import static sfs.util.Constantes.CONSTANTE_SUFIJO_ARCHIVO_LEYE;
import static sfs.util.Constantes.CONSTANTE_SUFIJO_ARCHIVO_NOTA;
import static sfs.util.Constantes.CONSTANTE_SUFIJO_ARCHIVO_JSON;
import static sfs.util.Constantes.CONSTANTE_SUFIJO_ARCHIVO_XML;
import static sfs.util.Constantes.CONSTANTE_TIP_ARCH_JSON;
import static sfs.util.Constantes.CONSTANTE_TIP_ARCH_TEXT;
import static sfs.util.Constantes.CONSTANTE_TIP_ARCH_XML;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_NDEBITO;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_NCREDITO;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_FACTURA;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_BOLETA;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_RBAJAS;
import static sfs.util.Constantes.CONSTANTE_TIPO_FUNCION_AUTO;
import static sfs.util.Constantes.CONSTANTE_EXTENSION_BAJAS;
import static sfs.util.Constantes.CONSTANTE_EXTENSION_NOTAS;
import static sfs.util.Constantes.CONSTANTE_EXTENSION_CDP;
import static sfs.util.Constantes.CONSTANTE_EXTENSION_JSON;
import static sfs.util.Constantes.CONSTANTE_EXTENSION_XML;
import static sfs.util.Constantes.CONSTANTE_EXTENSION_COMPL_DETA;
import static sfs.util.Constantes.CONSTANTE_EXTENSION_COMPL_RELA;
import static sfs.util.Constantes.CONSTANTE_EXTENSION_COMPL_ACAB;
import static sfs.util.Constantes.CONSTANTE_EXTENSION_COMPL_ADET;
import static sfs.util.Constantes.CONSTANTE_EXTENSION_COMPL_LEYE;
import static sfs.util.Constantes.CONSTANTE_VERSION_SFS;
import sfs.util.FacturadorUtil;

@Service
public class BandejaDocumentosServiceImpl implements BandejaDocumentosService {
	
	private static final Log log = LogFactory.getLog(BandejaDocumentosServiceImpl.class);
	
	@Autowired
	private TxxxxDAO txxxxDAO;
	
	@Autowired
	private TxxxyDAO txxxyDAO;
	
	@Autowired
	private GenerarDocumentosService generarDocumentosService;
	
	@Autowired
	private ComunesService comunesService;
	
	@Override
	public void eliminarBandeja(TxxxxBean txxxxBean) throws Exception{

		txxxxDAO.eliminarBandeja(txxxxBean);
			
	}
	
	@Override
	public List<TxxxxBean> consultarBandejaComprobantesPorId(TxxxxBean txxxxBean) throws Exception{

		List<TxxxxBean> listaBandeja = txxxxDAO.consultarBandejaPorId(txxxxBean);
			
		return listaBandeja;
	}
		
	
	@Override 
	public void actualizarEstadoBandejaCdp(TxxxxBean txxxxBean) throws Exception{

		txxxxDAO.actualizarBandeja(txxxxBean);
	}
	
	@Override
	public List<TxxxxBean> consultarBandejaComprobantes() throws Exception{
		TxxxxBean txxxxBean = new TxxxxBean();
		List<TxxxxBean> listaBandeja = txxxxDAO.consultarBandeja(txxxxBean);
			
		return listaBandeja;
	}
	
	@Override
	public void cargarArchivosContribuyente() throws Exception{
		TxxxxBean txxxxBeanBusq = null; 
		File listaArchivos = new File(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA));
		String[] totalArchivos = listaArchivos.list();
		String nombreArchivo, archivo;
		
		if (totalArchivos == null){
			String error = "Debe crear el directorio: " + comunesService.obtenerRutaTrabajo(CONSTANTE_DATA);
			log.error(error);
			throw new Exception(error);
		}else { 
		  for (int x=0;x<totalArchivos.length;x++){
			  /* Generar el Archivo xml en base al archivo plano para bajas */
			  archivo = totalArchivos[x].toUpperCase();
			  if(this.validarNombreArchivo(archivo)){
				  if(archivo.contains(CONSTANTE_SUFIJO_ARCHIVO_BAJA)){
					  nombreArchivo = archivo;
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(nombreArchivo);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_TEXT);
					  this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
				  }
				  
				  /* Generar el Archivo xml en base a archivos plano distintos a bajas */
				  if(archivo.contains(CONSTANTE_SUFIJO_ARCHIVO_CABE)){
					  nombreArchivo = archivo;
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(nombreArchivo);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_TEXT);
					  this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
				  }		  
				  
				  if(archivo.contains(CONSTANTE_SUFIJO_ARCHIVO_NOTA)){
					  nombreArchivo = archivo;
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(nombreArchivo);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_TEXT);
					  this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
				  }		  
				  
				  
				  /* Generar el Archivo XML en base al archivo json */
				  if(archivo.contains(CONSTANTE_SUFIJO_ARCHIVO_JSON)){
					  nombreArchivo = archivo.replace(CONSTANTE_SUFIJO_ARCHIVO_JSON,"");
					  
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(nombreArchivo);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_JSON);
					  this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
					 
				  }
				  
				  /* Generar el Archivo XML en base al archivo json */
				  if(archivo.contains(CONSTANTE_SUFIJO_ARCHIVO_XML)){
					  nombreArchivo = archivo.replace(CONSTANTE_SUFIJO_ARCHIVO_XML,"");
					  
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(nombreArchivo);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_XML_VALIDAR);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_XML);
					  this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
					 
				  }	 
			  }
		  }
		    
		}
	}
	
	
	@Override
	public void cargarArchivoContribuyente() throws Exception{
		TxxxxBean txxxxBeanBusq = null; 
		File listaArchivos = new File(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA));
		String[] totalArchivos = listaArchivos.list();
		String nombreArchivo, archivo;
		
		if (totalArchivos == null){
			String error = "Debe crear el directorio: " + comunesService.obtenerRutaTrabajo(CONSTANTE_DATA);
			log.error(error);
			throw new Exception(error);
		}else { 
			Boolean inserto = false;
			/* Generar el Archivo xml en base al archivo plano para bajas */
			for (int x=0;x<totalArchivos.length;x++){
			  archivo = totalArchivos[x].toUpperCase();
			  if(this.validarNombreArchivo(archivo)){
				  if(archivo.contains(CONSTANTE_SUFIJO_ARCHIVO_BAJA)){
					  nombreArchivo = archivo;
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(nombreArchivo);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_TEXT);
					  inserto = this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
				  }
				  
				  /* Generar el Archivo xml en base a archivos plano distintos a bajas */
				  if(archivo.contains(CONSTANTE_SUFIJO_ARCHIVO_CABE)){
					  nombreArchivo = archivo;
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(nombreArchivo);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_TEXT);
					  inserto = this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
				  }		  
				  
				  if(archivo.contains(CONSTANTE_SUFIJO_ARCHIVO_NOTA)){
					  nombreArchivo = archivo;
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(nombreArchivo);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_TEXT);
					  inserto = this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
				  }		  
				  
				  
				  /* Generar el Archivo XML en base al archivo json */
				  if(archivo.contains(CONSTANTE_SUFIJO_ARCHIVO_JSON)){
					  nombreArchivo = archivo.replace(CONSTANTE_SUFIJO_ARCHIVO_JSON,"");
					  
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(nombreArchivo);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_JSON);
					  inserto = this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
					 
				  }
				  
				  /* Generar el Archivo XML en base al archivo json */
				  if(archivo.contains(CONSTANTE_SUFIJO_ARCHIVO_XML)){
					  nombreArchivo = archivo.replace(CONSTANTE_SUFIJO_ARCHIVO_XML,"");
					  
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(nombreArchivo);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_XML_VALIDAR);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_XML);
					  inserto = this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
					 
				  }
				  
				  if(inserto) break;
				  
			  }
		  }
		    
		}
	}
	
	@Override
	public void cargarArchivoContribuyente(String directorio, String archivoProcesar) throws Exception{
	  TxxxxBean txxxxBeanBusq = null; 
	  
	  File listaArchivos = new File(directorio);
	  String[] totalArchivos = listaArchivos.list();
	  String archivoEnDirectorio="";
	  
	  for (int x=0;x<totalArchivos.length;x++){
		  archivoEnDirectorio = totalArchivos[x].toUpperCase();
		  String[] archivoTrabajo = archivoEnDirectorio.split("\\."); 
		  if(archivoProcesar.equals(archivoTrabajo[0])){
			  if(archivoTrabajo.length > 2)
				  throw new Exception("El nombre del archivo no es el correcto o esta mal formado.");
			  
			  if(this.validarNombreArchivo(archivoEnDirectorio)){
				  if(archivoEnDirectorio.contains(CONSTANTE_SUFIJO_ARCHIVO_BAJA)){
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(archivoProcesar);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_TEXT);
					  this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
					  break;
				  }
				  
				  /* Generar el Archivo xml en base a archivos plano distintos a bajas */
				  if(archivoEnDirectorio.contains(CONSTANTE_SUFIJO_ARCHIVO_CABE)){
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(archivoProcesar);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_TEXT);
					  this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
					  break;
				  }		  
				  
				  if(archivoEnDirectorio.contains(CONSTANTE_SUFIJO_ARCHIVO_NOTA)){
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(archivoProcesar);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_TEXT);
					  this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
					  break;
				  }		  
				  
				  
				  /* Generar el Archivo XML en base al archivo json */
				  if(archivoEnDirectorio.contains(CONSTANTE_SUFIJO_ARCHIVO_JSON)){
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(archivoProcesar);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_POR_GENERAR_XML);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_JSON);
					  this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
					  break;
					 
				  }
				  
				  /* Generar el Archivo XML en base al archivo json */
				  if(archivoEnDirectorio.contains(CONSTANTE_SUFIJO_ARCHIVO_XML)){
					  txxxxBeanBusq = new TxxxxBean();
					  txxxxBeanBusq.setNom_arch(archivoProcesar);
					  txxxxBeanBusq.setInd_situ(CONSTANTE_SITUACION_XML_VALIDAR);
					  txxxxBeanBusq.setTip_arch(CONSTANTE_TIP_ARCH_XML);
					  this.insertarRegistroBandejaDocumentos(txxxxBeanBusq);
					  break;
					 
				  }
			  	}
			  
		  }else{
			  
			  continue;
		  }
	  }
	}
	
	private Boolean insertarRegistroBandejaDocumentos(TxxxxBean txxxxBean) throws Exception{
		String nroDocumento="",numRuc="",tipoDocumento="", nombreArchivoCarga="";
		String nombreArchivoOrigen = txxxxBean.getNom_arch();
		String tipoArchivo = txxxxBean.getTip_arch();
		Boolean archivoResumenBaja = false, archivoFacturas = false;
		Boolean nuevoRegistro = false;
		// Si se trata de archivos de texto
		if(CONSTANTE_TIP_ARCH_TEXT.equals(tipoArchivo)){
			archivoResumenBaja = nombreArchivoOrigen.contains(CONSTANTE_SUFIJO_ARCHIVO_BAJA);
			archivoFacturas = nombreArchivoOrigen.contains(CONSTANTE_SUFIJO_ARCHIVO_CABE);
			
			if(archivoResumenBaja)
				nombreArchivoCarga = nombreArchivoOrigen.replace(CONSTANTE_SUFIJO_ARCHIVO_BAJA,"");
			else{
				if(archivoFacturas)			
					nombreArchivoCarga = nombreArchivoOrigen.replace(CONSTANTE_SUFIJO_ARCHIVO_CABE,"");
				else
					nombreArchivoCarga = nombreArchivoOrigen.replace(CONSTANTE_SUFIJO_ARCHIVO_NOTA,"");
			}
		}
		
		if(CONSTANTE_TIP_ARCH_JSON.equals(tipoArchivo))
			nombreArchivoCarga = nombreArchivoOrigen.replace(CONSTANTE_SUFIJO_ARCHIVO_JSON,"");
		
		if(CONSTANTE_TIP_ARCH_XML.equals(tipoArchivo))
			nombreArchivoCarga = nombreArchivoOrigen.replace(CONSTANTE_SUFIJO_ARCHIVO_XML,"");
			
		
		// Actualizando el Nombre Archivo
		txxxxBean.setNom_arch(nombreArchivoCarga);
		Integer retorno = txxxxDAO.contarBandejaPorNomArch(txxxxBean);
		String nombreArchivo[] = nombreArchivoCarga.split("\\-");
		if(retorno == 0 ){
			// Si el archivo a cargar es diferente de resumen de bajas
			if(!CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(nombreArchivo[1])){
				numRuc = nombreArchivo[0];
				tipoDocumento = nombreArchivo[1];
				nroDocumento = nombreArchivo[2]+"-"+nombreArchivo[3];
			}else{
				numRuc = nombreArchivo[0];
				tipoDocumento = nombreArchivo[1];
				nroDocumento = nombreArchivo[1]+"-"+nombreArchivo[2]+"-"+nombreArchivo[3];
			}
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String fechaActual = format.format(new Date());
			TxxxxBean txxxxBeanInsert = new TxxxxBean();
			txxxxBeanInsert.setNum_ruc(numRuc);
			txxxxBeanInsert.setTip_docu(tipoDocumento);
			txxxxBeanInsert.setNum_docu(nroDocumento);
			txxxxBeanInsert.setFec_carg(fechaActual);
			txxxxBeanInsert.setFec_envi("-");
			txxxxBeanInsert.setFec_gene("-");
			txxxxBeanInsert.setNom_arch(nombreArchivoCarga);
			txxxxBeanInsert.setDes_obse("-");
			txxxxBeanInsert.setInd_situ(txxxxBean.getInd_situ());
			txxxxBeanInsert.setTip_arch(txxxxBean.getTip_arch());
			txxxxDAO.insertarBandeja(txxxxBeanInsert);
			
			nuevoRegistro = true;
		} 
		
		return nuevoRegistro;
	}
	
	@Override
	public Map<String,Object> enviarComprobantePagoSunat(TxxxxBean txxxxBean) throws Exception{
		Map<String,Object> retorno = null;
		Map<String,String> resultadoWebService = null;
		String nombreArchivo="constantes.properties";
		
		if(CONSTANTE_SITUACION_XML_GENERADO.equals(txxxxBean.getInd_situ())||CONSTANTE_SITUACION_ENVIADO_RECHAZADO.equals(txxxxBean.getInd_situ())){
			retorno = new HashMap<String,Object>(); 
			Properties prop = new Properties();
			InputStream input = null;
			
			String rutaArchivoProperties = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO) + nombreArchivo;
			input = new FileInputStream(rutaArchivoProperties);
			prop.load(input);
			String urlWebService = prop.getProperty("RUTA_SERV_CDP")!=null?prop.getProperty("RUTA_SERV_CDP"):"XX";
			input.close();
			String tipoComprobante = txxxxBean.getTip_docu();
			String filename = txxxxBean.getNom_arch();
			String[] rutaUrl = urlWebService.split("\\/");
			comunesService.validarConexion(rutaUrl[2], 80);
			resultadoWebService = generarDocumentosService.enviarArchivoSunat(urlWebService,filename,tipoComprobante);
			
			retorno.put("resultadoWebService", resultadoWebService);
			
		}

		return retorno;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public String generarComprobantePagoSunat(TxxxxBean txxxxBean) throws Exception{ 
		String retorno = CONSTANTE_SITUACION_POR_GENERAR_XML;
		/* Validar si existe Archivo por Procesar */
		String[] archivos = new String[6];
		String tipoComprobante = null;
		StringBuilder archivoCabecera = new StringBuilder(),archivoDetalle = new StringBuilder();
		StringBuilder archivoRelacionado = new StringBuilder(),archivoAdiCabecera = new StringBuilder();
		StringBuilder archivoAdiDetalle = new StringBuilder(), archivoLeyenda = new StringBuilder();
		
		if(CONSTANTE_SITUACION_POR_GENERAR_XML.equals(txxxxBean.getInd_situ())||CONSTANTE_SITUACION_CON_ERRORES.equals(txxxxBean.getInd_situ())||
		   CONSTANTE_SITUACION_XML_VALIDAR.equals(txxxxBean.getInd_situ())||CONSTANTE_SITUACION_ENVIADO_RECHAZADO.equals(txxxxBean.getInd_situ())){
			retorno = "";
			tipoComprobante = txxxxBean.getTip_docu();
			if(CONSTANTE_TIP_ARCH_TEXT.equalsIgnoreCase(txxxxBean.getTip_arch())){
				if(!"".equals(tipoComprobante)){
					
					if(CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(tipoComprobante)){
						archivoCabecera.setLength(0);
						archivoCabecera.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                   .append(txxxxBean.getNom_arch())
					                   .append(CONSTANTE_SUFIJO_ARCHIVO_BAJA);
						
						archivos[0] = archivoCabecera.toString();
					}
										
					if(CONSTANTE_TIPO_DOCUMENTO_FACTURA.equals(tipoComprobante)||CONSTANTE_TIPO_DOCUMENTO_BOLETA.equals(tipoComprobante)){
						archivoCabecera.setLength(0);
						archivoCabecera.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                   .append(txxxxBean.getNom_arch())
					                   .append(CONSTANTE_SUFIJO_ARCHIVO_CABE);
					  
						archivoDetalle.setLength(0);
						archivoDetalle.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                  .append(txxxxBean.getNom_arch())
					                  .append(CONSTANTE_SUFIJO_ARCHIVO_DETA);	 	
						
						archivoRelacionado.setLength(0);
						archivoRelacionado.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                      .append(txxxxBean.getNom_arch())
					                      .append(CONSTANTE_SUFIJO_ARCHIVO_RELA);
					   	
						archivoAdiCabecera.setLength(0);
						archivoAdiCabecera.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                      .append(txxxxBean.getNom_arch())
					                      .append(CONSTANTE_SUFIJO_ARCHIVO_ACAB);
						
						
						archivoAdiDetalle.setLength(0);
						archivoAdiDetalle.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                     .append(txxxxBean.getNom_arch())
					                     .append(CONSTANTE_SUFIJO_ARCHIVO_ADET);
						
						archivoLeyenda.setLength(0);
						archivoLeyenda.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                  .append(txxxxBean.getNom_arch())
					                  .append(CONSTANTE_SUFIJO_ARCHIVO_LEYE);
						
						archivos[0] = archivoCabecera.toString();
						archivos[1] = archivoDetalle.toString();
						archivos[2] = archivoRelacionado.toString();
						archivos[3] = archivoAdiCabecera.toString();
						archivos[4] = archivoAdiDetalle.toString();
						archivos[5] = archivoLeyenda.toString();
						
					}
						
					
					if(CONSTANTE_TIPO_DOCUMENTO_NCREDITO.equals(tipoComprobante)||CONSTANTE_TIPO_DOCUMENTO_NDEBITO.equals(tipoComprobante)){
						archivoCabecera.setLength(0);
						archivoCabecera.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                   .append(txxxxBean.getNom_arch())
					                   .append(CONSTANTE_SUFIJO_ARCHIVO_NOTA);
					  
						archivoDetalle.setLength(0);
						archivoDetalle.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                  .append(txxxxBean.getNom_arch())
					                  .append(CONSTANTE_SUFIJO_ARCHIVO_DETA);	 
						
						archivoRelacionado.setLength(0);
						archivoRelacionado.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                      .append(txxxxBean.getNom_arch())
					                      .append(CONSTANTE_SUFIJO_ARCHIVO_RELA);
					   	
						archivoAdiCabecera.setLength(0);
						archivoAdiCabecera.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                      .append(txxxxBean.getNom_arch())
					                      .append(CONSTANTE_SUFIJO_ARCHIVO_ACAB);
						
						
						archivoAdiDetalle.setLength(0);
						archivoAdiDetalle.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                     .append(txxxxBean.getNom_arch())
					                     .append(CONSTANTE_SUFIJO_ARCHIVO_ADET);
						
						archivoLeyenda.setLength(0);
						archivoLeyenda.append(comunesService.obtenerRutaTrabajo(CONSTANTE_DATA))
					                  .append(txxxxBean.getNom_arch())
					                  .append(CONSTANTE_SUFIJO_ARCHIVO_LEYE);

						
						archivos[0] = archivoCabecera.toString();
						archivos[1] = archivoDetalle.toString();
						archivos[2] = archivoRelacionado.toString();
						archivos[3] = archivoAdiCabecera.toString();
						archivos[4] = archivoAdiDetalle.toString();
						archivos[5] = archivoLeyenda.toString();
					}
				}
				
			   generarDocumentosService.formatoPlantillaXml(tipoComprobante, archivos, txxxxBean.getNom_arch());
			   generarDocumentosService.validarSchemaXML(tipoComprobante, comunesService.obtenerRutaTrabajo(CONSTANTE_TEMP) + txxxxBean.getNom_arch() + ".xml");
			   generarDocumentosService.validarXML(tipoComprobante,comunesService.obtenerRutaTrabajo(CONSTANTE_TEMP),txxxxBean.getNom_arch());
			  
			}
			
			if(CONSTANTE_TIP_ARCH_JSON.equalsIgnoreCase(txxxxBean.getTip_arch())){
				String archivoJson = comunesService.obtenerRutaTrabajo(CONSTANTE_DATA)+txxxxBean.getNom_arch()+CONSTANTE_SUFIJO_ARCHIVO_JSON;
				String fileJson = leerArchivoJson(archivoJson);
				HashMap<String,Object> objectoJson =  new ObjectMapper().readValue(fileJson, HashMap.class);
				generarDocumentosService.formatoJsonPlantilla(objectoJson, txxxxBean.getNom_arch());
				generarDocumentosService.validarSchemaXML(tipoComprobante, comunesService.obtenerRutaTrabajo(CONSTANTE_TEMP) + txxxxBean.getNom_arch() + ".xml");
				generarDocumentosService.validarXML(tipoComprobante,comunesService.obtenerRutaTrabajo(CONSTANTE_TEMP), txxxxBean.getNom_arch());
			}
			
			if(CONSTANTE_TIP_ARCH_XML.equalsIgnoreCase(txxxxBean.getTip_arch())){
				generarDocumentosService.adicionarInformacionFacturador(txxxxBean.getNom_arch());
				generarDocumentosService.validarSchemaXML(tipoComprobante, comunesService.obtenerRutaTrabajo(CONSTANTE_TEMP) + txxxxBean.getNom_arch() + ".xml");
				generarDocumentosService.validarXML(tipoComprobante,comunesService.obtenerRutaTrabajo(CONSTANTE_TEMP), txxxxBean.getNom_arch());
			}
			
			generarDocumentosService.firmarComprimirXml(txxxxBean.getNom_arch());
			
			
		}
		
		return retorno;
	}
	
	
	private String leerArchivoJson(String nombreArchivo) throws Exception {
	   
		BufferedReader br = new BufferedReader(new FileReader(nombreArchivo));
        StringBuilder sb = new StringBuilder();
        String linea = br.readLine();

        while (linea != null) {
            sb.append(linea);
            linea = br.readLine();
        }
        br.close();

        return sb.toString();

	}
	
	@Override
	public String listarCertificados(Map<String, Object> obj) throws Exception{
		Map<String,Object> resultado = new HashMap<String,Object>();
		Map<String,String> archivoLista = null;
		String retorno = "";
		List<Map<String,String>> listaArchivos = new ArrayList<Map<String,String>>();
		Integer error = 0;
		
		File directorioCertificados = new File(comunesService.obtenerRutaTrabajo(CONSTANTE_CERT));
		
		if(!directorioCertificados.exists()){
			resultado.put("validacion","No existe el directorio.");
			resultado.put("adjunto",null);
			error = 1;
		}
		
		if(!directorioCertificados.isDirectory()&&error==0){
			resultado.put("validacion",comunesService.obtenerRutaTrabajo(CONSTANTE_CERT) + ", no es directorio.");
			resultado.put("adjunto",null);
			error = 1;
		}
		
		if(error == 0){
			String[] archivos = directorioCertificados.list();	
			
			if (archivos == null){
				resultado.put("validacion","No hay ficheros en el directorio especificado");
				resultado.put("adjunto",null);
				error = 1;
			}else{
				for (int x=0;x<archivos.length;x++){
					archivoLista = new HashMap<String,String>(); 
					archivoLista.put("id", archivos[x]);
					archivoLista.put("nombre", archivos[x]);
					
					listaArchivos.add(archivoLista);
				}
								
				resultado.put("validacion","");
				resultado.put("adjunto",listaArchivos);
			}
		}
		
		
		ObjectMapper mapper = new ObjectMapper();
		retorno = mapper.writeValueAsString(resultado);
				
		return retorno;
	}
	
	@Override
	public String importarCertificado(Map<String, Object> obj) throws Exception{
	String retorno = "";
	String numRuc = "";
	String aliasPfx = "";
	Integer error = 0;
	Map<String,String> resultado = new HashMap<String,String>();
	String nombreCertificado = obj.get("nombreCertificado")!=null?(String)obj.get("nombreCertificado"):"";
	String passPrivateKey =  obj.get("passPrivateKey")!=null?(String)obj.get("passPrivateKey"):"";
	String rutaCertificado = comunesService.obtenerRutaTrabajo(CONSTANTE_CERT) + nombreCertificado;
	
	resultado.put("validacion", "EXITO");
	if("".equals(rutaCertificado)){
		resultado.put("validacion", "Debe ingresar la ruta del certificado");
		error = 1;
	}
	
	if (rutaCertificado.indexOf(".pfx") == -1 && error == 0){
		resultado.put("validacion", "Archivo cargado debe ser de tipo \"pfx\" ");
		error = 1;
	}
	
	if ("".equals(passPrivateKey) && error == 0){
		resultado.put("validacion", "Debe ingresar su contraseña de certificado");
		error = 1;
	}
			
	// Validar el certificado
	if (error == 0){
		List<TxxxyBean> listado = new ArrayList<TxxxyBean>();
		// Buscar Parametro
		TxxxyBean parametro = new TxxxyBean();
		parametro.setId_para("PARASIST");
		parametro.setCod_para("NUMRUC");
		try {
			listado = txxxyDAO.consultarParametro(parametro);
		} catch (Exception e) {
			log.error("Mensaje de Error: " + e.getMessage());
			resultado.put("validacion","Mensaje de Error: " + e.getMessage());
			error = 1;
		}
		
		if(error == 0){
			if( listado.size() > 0)	numRuc = listado.get(0).getVal_para();
			
			if ("".equals(numRuc)){
				resultado.put("validacion","No Existe valor parametrizado del RUC");
				error = 1;
			}
		}
		
	}
	
	if (error == 0){
		try{
			// Variable aliasPfx es pasada por referencia
			String output = generarDocumentosService.validaCertificado(rutaCertificado,numRuc,passPrivateKey);
			if(!output.contains("[ALIAS]")){
				resultado.put("validacion","Certificado, no esta configurado con el valor del RUC");
				error = 1;
			}else{
				Integer position = output.indexOf(":") + 1;
				aliasPfx = output.substring(position);
			}				
		}catch(Exception e){
			log.error("Mensaje de Error: " + e.getMessage());
		}
	}
	
	
	if (error == 0){
		String salida = FacturadorUtil.executeCommand("keytool -delete -alias certContribuyente -storepass SuN@TF4CT -keystore \"" + comunesService.obtenerRutaTrabajo(CONSTANTE_ALMCERT) + "FacturadorKey.jks\"");
		salida = FacturadorUtil.executeCommand("keytool -importkeystore -srcalias "+aliasPfx+" -srckeystore "+rutaCertificado+" -srcstoretype pkcs12 -srcstorepass "+passPrivateKey+" -destkeystore " + comunesService.obtenerRutaTrabajo(CONSTANTE_ALMCERT) + "FacturadorKey.jks -deststoretype JKS -destalias certContribuyente -deststorepass SuN@TF4CT");
		if(!"".equals(salida)){
			resultado.put("validacion","Hubo un error, el certificado no fue creado");
			error = 1;
		}	
		
	}
	
	
	// Grabar nombre del certificado
	if (error == 0){
		List<TxxxyBean> listado = new ArrayList<TxxxyBean>(); 
		TxxxyBean txxxyBean = new TxxxyBean();
		txxxyBean.setId_para("PARASIST");
		txxxyBean.setCod_para("NOMCERT");
		try {
			listado = txxxyDAO.consultarParametro(txxxyBean);
		} catch (Exception e) {
			log.error("Error : " + e.getMessage());
		}
		if( listado.size() > 0){
			txxxyBean.setVal_para(nombreCertificado);
			txxxyDAO.actualizarParametro(txxxyBean);
		}else{
			txxxyBean.setNom_para("Nombre del Certificado");
			txxxyBean.setTip_para("CADENA");
			txxxyBean.setVal_para(nombreCertificado);
			txxxyBean.setInd_esta_para("1");
			txxxyDAO.insertarParametro(txxxyBean);
		}
	}
	
	// Grabar Contraseña del certificado
	if (error == 0){
		List<TxxxyBean> listado = new ArrayList<TxxxyBean>(); 
		TxxxyBean txxxyBean = new TxxxyBean();
		txxxyBean.setId_para("PARASIST");
		txxxyBean.setCod_para("PRKCRT");
		try {
			listado = txxxyDAO.consultarParametro(txxxyBean);
		} catch (Exception e) {
			log.error("Error : " + e.getMessage());
		}
		if( listado.size() > 0){
			txxxyBean.setVal_para(generarDocumentosService.Encriptar(passPrivateKey));
			txxxyDAO.actualizarParametro(txxxyBean);
		}else{
			txxxyBean.setNom_para("Contraseña del Certificado Emisor");
			txxxyBean.setTip_para("CADENA");
			txxxyBean.setVal_para(generarDocumentosService.Encriptar(passPrivateKey));
			txxxyBean.setInd_esta_para("1");
			txxxyDAO.insertarParametro(txxxyBean);
		}
	}
	
	
	ObjectMapper mapper = new ObjectMapper();
	try {
		retorno = mapper.writeValueAsString(resultado);
	} catch (Exception e) {
		
		log.error(retorno);

	}
			
	return retorno;
	}
		
	@Override
	public String obtenerParametro(Map<String, Object> obj) throws Exception{
		String retorno = "";
		Map<String,Object> resultado = new HashMap<String,Object>();
		TxxxyBean paramtroBean = new TxxxyBean();
		String numRuc = "", usuarioSol = "", claveSol = "", funcionamiento="", certificado = "";
		String param = "", razonSocial = "", tiempoGenera="", rutaSolucion = "", tiempoEnvia="";
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = null;
		try {
			listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
			if(listaParametros.size() > 0){
				for(TxxxyBean parametro : listaParametros ){
					param = parametro.getVal_para();
					if("NUMRUC".equals(parametro.getCod_para())) numRuc = param;
					if("USUSOL".equals(parametro.getCod_para())) usuarioSol = param;
					if("CLASOL".equals(parametro.getCod_para())) claveSol = generarDocumentosService.Desencriptar(param);
					if("NOMCERT".equals(parametro.getCod_para())) certificado = param;
					if("RUTSOL".equals(parametro.getCod_para())) rutaSolucion = param;
					if("FUNCIO".equals(parametro.getCod_para())) funcionamiento = param;
					if("RAZON".equals(parametro.getCod_para())) razonSocial = param;
					if("TIMEGENERA".equals(parametro.getCod_para())) tiempoGenera = param;
					if("TIMEENVIA".equals(parametro.getCod_para())) tiempoEnvia = param;
				}
			}
			
			Map<String,String> parametros = new HashMap<String,String>();
			parametros.put("numRuc", numRuc);
			parametros.put("usuarioSol", usuarioSol);
			parametros.put("claveSol", claveSol);
			parametros.put("certificado", certificado);
			parametros.put("funcionamiento", funcionamiento);
			parametros.put("razonSocial", razonSocial);
			parametros.put("tiempoGenera", tiempoGenera);
			parametros.put("tiempoEnviar", tiempoEnvia);			
			parametros.put("rutaSolucion", rutaSolucion);
			
			resultado.put("validacion", "");
			resultado.put("adjunto", parametros);
			
			ObjectMapper mapper = new ObjectMapper();
			retorno = mapper.writeValueAsString(resultado);
		} catch (Exception e) {
			log.error(retorno);
		}
		
		return retorno;
		
	}
	
	@Override
	public String obtenerOtrosParametro(Map<String, Object> obj) throws Exception{
		String retorno = "";
		Map<String,Object> resultado = new HashMap<String,Object>();
		String nombreComercial="",ubigeo="",direccion="",departamento="",provincia="",distrito="",urbanizacion="",param="";
		TxxxyBean paramtroBean = new TxxxyBean();
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = null;
		try {
			listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
			if(listaParametros.size() > 0){
				for(TxxxyBean parametro : listaParametros ){
					param = parametro.getVal_para();
					if("NOMCOM".equals(parametro.getCod_para())) nombreComercial = param;
					if("UBIGEO".equals(parametro.getCod_para())) ubigeo = param;
					if("DIRECC".equals(parametro.getCod_para())) direccion = param;
					if("DEPAR".equals(parametro.getCod_para())) departamento = param;
					if("PROVIN".equals(parametro.getCod_para())) provincia = param;
					if("DISTR".equals(parametro.getCod_para())) distrito = param;
					if("URBANIZA".equals(parametro.getCod_para())) urbanizacion = param;					
				}
			}
			
			Map<String,String> parametros = new HashMap<String,String>();
			parametros.put("nombreComercial", nombreComercial);
			parametros.put("ubigeo", ubigeo);
			parametros.put("direccion", direccion);
			parametros.put("departamento", departamento);
			parametros.put("provincia", provincia);
			parametros.put("distrito", distrito);
			parametros.put("urbanizacion", urbanizacion);
			
			resultado.put("validacion", "");
			resultado.put("adjunto", parametros);
			
			ObjectMapper mapper = new ObjectMapper();
			retorno = mapper.writeValueAsString(resultado);
		} catch (Exception e) {
			log.error(retorno);
		}
		
		return retorno;
		
	}
	
	@Override
	public String grabarParametro(Map<String, Object> obj) throws Exception{
		String retorno = "";Integer error = 0;
		Map<String,String> resultado = new HashMap<String,String>();
				
		String numRuc = obj.get("txtNumeroRuc")!=null?(String)obj.get("txtNumeroRuc"):"";
		String usuaSol = obj.get("txtUsuarioSol")!=null?(String)obj.get("txtUsuarioSol"):"";
		String claveSol = obj.get("txtClaveSol")!=null?(String)obj.get("txtClaveSol"):"";
		String cmbFuncion = obj.get("cmbFuncionamiento")!=null?(String)obj.get("cmbFuncionamiento"):"";
		String txtRazonSocial = obj.get("txtRazonSocial")!=null?(String)obj.get("txtRazonSocial"):"";
		String txtTiempoGenera = obj.get("cmbTiempoGenera")!=null?(String)obj.get("cmbTiempoGenera"):"";
		String txtTiempoEnviar = obj.get("cmbTiempoEnvia")!=null?(String)obj.get("cmbTiempoEnvia"):"";
		String txtRutaSolucion = obj.get("txtRutaSolucion")!=null?(String)obj.get("txtRutaSolucion"):"";
				
		/* Validando datos */
		resultado.put("validacion", "EXITO");
		if("".equals(numRuc)){
			resultado.put("validacion", "Debe ingresar el nro de RUC");
			error = 1;
		}
		
		if(numRuc.length()!=11&&(error==0)){
			resultado.put("validacion", "El RUC debe ser de 11 caracteres");
			error = 1;
		}
		
		if("".equals(usuaSol)&&(error==0)){
			resultado.put("validacion", "Debe ingresar su usuario SOL");
			error = 1;
		}
		
		if("".equals(claveSol)&&(error==0)){
			resultado.put("validacion", "Debe ingresar su clave SOL");
			error = 1;
		}
		
		if("".equals(cmbFuncion)&&(error==0)){
			resultado.put("validacion", "Debe elegir si usar o no temporizador.");
			error = 1;
		}
		
		if(CONSTANTE_TIPO_FUNCION_AUTO.equals(cmbFuncion)&&(error==0)){
			/* Validar que no este en blanco */	
			if("".equals(txtTiempoGenera)){
				resultado.put("validacion", "Debe seleccionar el tiempo del temporizador para generar comprobante.");
				error = 1;
			}
		}	
				
		if("".equals(txtRazonSocial)&&(error==0)){
			resultado.put("validacion", "Debe ingresar su Razon Social o Apellidos y Nombres.");
			error = 1;
		}
		
		
		/* Grabando Parametros*/
		if(error == 0){
			
			TxxxyBean txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("NUMRUC");
			List<TxxxyBean> listado = null;
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(numRuc);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Ruc del Contribuyente Emisor");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(numRuc);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
						
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("USUSOL");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(usuaSol);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Usuario SOL del Contribuyente Emisor");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(usuaSol);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("CLASOL");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(generarDocumentosService.Encriptar(claveSol));
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Clave SOL del Contribuyente Emisor");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(generarDocumentosService.Encriptar(claveSol));
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("FUNCIO");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(cmbFuncion);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Tipo Funcionamiento del facturador");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(cmbFuncion);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("TIMEGENERA");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(txtTiempoGenera);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Valor Temporizador del facturador - Generar");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(txtTiempoGenera);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("TIMEENVIA");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(txtTiempoEnviar);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Valor Temporizador del facturador - Enviar");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(txtTiempoEnviar);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("RAZON");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(txtRazonSocial);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Razon Social o Nombres");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(txtRazonSocial);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("RUTSOL");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(txtRutaSolucion);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Ruta de la solución de software");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(txtRutaSolucion);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}				
			
		}	
		
		/*Actualizar Archivo de Propiedades*/
		String linea = "";
		StringBuilder rutaBase = new StringBuilder();
		rutaBase.setLength(0);
		rutaBase.append(System.getenv("SUNAT_HOME"))
				.append("/servers/sfs/util/scheduler.properties");
		
		StringBuilder rutaFinal = new StringBuilder();
		rutaFinal.setLength(0);
		rutaFinal.append(System.getenv("SUNAT_HOME"))
				 .append("/servers/sfs/util/scheduler_temp.properties");
				
		FileReader fileReader = new FileReader(rutaBase.toString());
		FileWriter fileWrite  = new FileWriter(rutaFinal.toString());
        
		BufferedReader br = new BufferedReader(fileReader);
		BufferedWriter bw = new BufferedWriter(fileWrite);
        while ((linea = br.readLine()) != null) {
		     if (linea.contains("TiempoGenComprobante")){
		    	 if ("".equals(txtTiempoEnviar))
    		    	 linea = "TiempoGenComprobante=0 0 8 * * *";
    			 else
    				 linea = "TiempoGenComprobante=0/"+txtTiempoGenera+" * * * * *";
		     }
		     
    		 if(linea.contains("TiempoEnvComprobante")){
    			 if ("".equals(txtTiempoEnviar))
    		    	 linea = "TiempoEnvComprobante=0 0 8 * * *";
    			 else
        			 linea = "TiempoEnvComprobante=0 0/"+txtTiempoEnviar+" * * * *";
    		 }

		     bw.write(linea+"\n");
        }  
        
        if(br != null)
            br.close();
        
        if(bw != null)
        	bw.close();
        
        File oldFile = new File(rutaBase.toString());
        oldFile.delete();

        File newFile = new File(rutaFinal.toString());
        newFile.renameTo(oldFile);

		
		ObjectMapper mapper = new ObjectMapper();
		try {
			retorno = mapper.writeValueAsString(resultado);
		} catch (Exception e) {
			log.error(retorno);
		}
		
		return retorno;
		
	}
	
	@Override
	public String grabarOtrosParametro(Map<String, Object> obj) throws Exception{
		String retorno = "";Integer error = 0;
		Map<String,String> resultado = new HashMap<String,String>();
						
		String nombreComercial = obj.get("txtNombreComercial")!=null?(String)obj.get("txtNombreComercial"):"";
		String ubigeo = obj.get("txtUbigeo")!=null?(String)obj.get("txtUbigeo"):"";
		String direccion = obj.get("txtDireccion")!=null?(String)obj.get("txtDireccion"):"";
		String departamento = obj.get("txtDepartamento")!=null?(String)obj.get("txtDepartamento"):"";
		String provincia = obj.get("txtProvincia")!=null?(String)obj.get("txtProvincia"):"";
		String distrito = obj.get("txtDistrito")!=null?(String)obj.get("txtDistrito"):"";
		String urbanizacion = obj.get("txtUrbanizacion")!=null?(String)obj.get("txtUrbanizacion"):"";
				
		/* Validando datos */
		resultado.put("validacion", "EXITO");
		if("".equals(nombreComercial)){
			resultado.put("validacion", "Debe ingresar el nombre comercial");
			error = 1;
		}
		
		if("".equals(ubigeo)&&(error==0)){
			resultado.put("validacion", "Debe ingresar el UBIGEO");
			error = 1;
		}
		
		if("".equals(direccion)&&(error==0)){
			resultado.put("validacion", "Debe ingresar direccion");
			error = 1;
		}
		
		if("".equals(departamento)&&(error==0)){
			resultado.put("validacion", "Debe ingresar departamento de UBIGEO.");
			error = 1;
		}
		
		if("".equals(provincia)&&(error==0)){
			resultado.put("validacion", "Debe ingresar provincia de UBIGEO.");
			error = 1;
		}
		
		if("".equals(distrito)&&(error==0)){
			resultado.put("validacion", "Debe ingresar distrito de UBIGEO.");
			error = 1;
		}
		
		if("".equals(urbanizacion)&&(error==0)){
			resultado.put("validacion", "Debe ingresar urbanizacion de UBIGEO.");
			error = 1;
		}
		
		/* Grabando Parametros*/
		if(error == 0){
			
			TxxxyBean txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("NOMCOM");
			List<TxxxyBean> listado = null;
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(nombreComercial);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Nombre Comercial del Emisor");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(nombreComercial);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
						
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("UBIGEO");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(ubigeo);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Ubigeo Dirección del Emisor");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(ubigeo);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("DIRECC");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(direccion);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Dirección del Emisor");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(direccion);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("DEPAR");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(departamento);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Departamento direccion de Emisor");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(departamento);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("PROVIN");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(provincia);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Provincia direccion de Emisor");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(provincia);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("DISTR");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(distrito);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Distrito direccion de Emisor");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(distrito);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
			txxxyBean = new TxxxyBean();
			txxxyBean.setId_para("PARASIST");
			txxxyBean.setCod_para("URBANIZA");
			try {
				listado = txxxyDAO.consultarParametro(txxxyBean);
			} catch (Exception e) {
				log.error("Error : " + e.getMessage());
			}
			if( listado.size() > 0){
				txxxyBean.setVal_para(urbanizacion);
				txxxyDAO.actualizarParametro(txxxyBean);
			}else{
				txxxyBean.setNom_para("Urbanizacion de direccion de Emisor");
				txxxyBean.setTip_para("CADENA");
				txxxyBean.setVal_para(urbanizacion);
				txxxyBean.setInd_esta_para("1");
				txxxyDAO.insertarParametro(txxxyBean);
			}
			
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			retorno = mapper.writeValueAsString(resultado);
		} catch (Exception e) {
			log.error(retorno);
		}
		
		return retorno;
		
	}
	
	@Override
	public List<TxxxxBean> buscarBandejaPorSituacion(String situacionComprobante) throws Exception{
		TxxxxBean txxxxBeanEnviar = new TxxxxBean();
		txxxxBeanEnviar.setInd_situ(situacionComprobante);
		List<TxxxxBean> listaPendEnvio = txxxxDAO.consultarBandejaPorSituacion(txxxxBeanEnviar);
		
		return listaPendEnvio;
		
	}
	
	
	@Override
	public List<TxxxyBean> consultarParametro(TxxxyBean txxxyBean) throws Exception{
	
		List<TxxxyBean> listado = txxxyDAO.consultarParametro(txxxyBean);
		
		return listado;
	
	}
	
	
	@Override
	public String validarParametroRegistrado() throws Exception{
		
		String mensaje="";
		/* Validar la versión del facturador */
		Boolean resultado = comunesService.validarVersionFacturador(CONSTANTE_VERSION_SFS);
		
		
		if(!resultado)
			mensaje = "La versión de su facturador se encuentra desactualizada, no podrá continuar.";
		
		/* Cargando Lista con Parametros Obligatorios */
		List<String> listaParametroValidar = new ArrayList<String>();
		listaParametroValidar.add("NUMRUC");
		listaParametroValidar.add("USUSOL");
		listaParametroValidar.add("CLASOL");
		listaParametroValidar.add("RUTSOL");
		listaParametroValidar.add("PRKCRT");
		listaParametroValidar.add("FUNCIO");
		listaParametroValidar.add("RAZON");
		listaParametroValidar.add("NOMCOM");
		listaParametroValidar.add("UBIGEO");
		listaParametroValidar.add("DIRECC");
		listaParametroValidar.add("DEPAR");
		listaParametroValidar.add("PROVIN");
		listaParametroValidar.add("DISTR");
		listaParametroValidar.add("URBANIZA");
		listaParametroValidar.add("TIEMPO");
		/* Comparando Listas */
		TxxxyBean paramtroBean = new TxxxyBean();
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
 		try {
 			List<TxxxyBean> listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
 			
 			List<String> listaParametrosExistentes = new ArrayList<String>();
 			for(TxxxyBean txxxyBean : listaParametros)
 				listaParametrosExistentes.add(txxxyBean.getCod_para());
 			
 			for(String codigo : listaParametroValidar){
 				if(!listaParametrosExistentes.contains(codigo)){
 					if("NUMRUC".equals(codigo))
 						mensaje = "Debe ingresar el parámetro RUC en la opción configuración del facturador."; 	
 					
 					if("USUSOL".equals(codigo))
 						mensaje = "Debe ingresar el parámetro usuario secundario en la opción configuración del facturador.";
 					
 					if("CLASOL".equals(codigo))
 						mensaje = "Debe ingresar el parámetro clave SOL en la opción configuración del facturador.";
 					
 					if("RUTSOL".equals(codigo))
 						mensaje = "Debe importar su certificado digital.";
 					
 					if("PRKCRT".equals(codigo))
 						mensaje = "Debe importar su certificado digital.";
 					
 					if("FUNCIO".equals(codigo))
 						mensaje = "Debe ingresar el parámetro tipo de funcionamiento con temporizador o no, en la opción configuración del facturador.";
 					
 					if("TIMEGENERA".equals(codigo))
 						mensaje = "Debe ingresar el parámetro de tiempo de generación en la opción configuración del facturador.";
 					
 					if("TIMEENVIA".equals(codigo))
 						mensaje = "Debe ingresar el parámetro de tiempo de envío en la opción configuración del facturador.";
 					
 					if("RAZON".equals(codigo))
 						mensaje = "Debe ingresar el parámetro de razón social en la opción configuración del facturador.";
 					
 					if("NOMCOM".equals(codigo))
 						mensaje = "Debe ingresar el parámetro de nombre completo en la opción configuración del facturador.";
 					
 					if("UBIGEO".equals(codigo))
 						mensaje = "Debe ingresar el parámetro de ubigeo en la opción configuración del facturador.";
 					
 					if("DIRECC".equals(codigo))
 						mensaje = "Debe ingresar el parámetro de dirección en la opción configuración del facturador.";
 					
 					if("DEPAR".equals(codigo))
 						mensaje = "Debe ingresar el parámetro de departamento en la opción configuración del facturador.";
 					
 					if("PROVIN".equals(codigo))
 						mensaje = "Debe ingresar el parámetro de provincia en la opción configuración del facturador.";
 					
 					if("URBANIZA".equals(codigo))
 						mensaje = "Debe ingresar el parámetro de urbanización en la opción configuración del facturador.";					
 					
 					break;
 				}
 			}
			
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
 		
		return mensaje;
		
	}
	
	private Boolean validarNombreArchivo(String nombreArchivo) throws Exception{
		Boolean resultado = true;
		
		/* Validar que no sea directorio */
		File directorio = new File(nombreArchivo);
		if(directorio.isDirectory())			
			resultado = false;
		
		
		/* Validar que no exista mas de un punto en el nombre del archivo */
		String[] validacionPunto = nombreArchivo.split("\\.");
		if(validacionPunto.length != 2)
			resultado = false;
		
		/* Validar que sea una extension valida */
		if(resultado){
			String tipoExtension = validacionPunto[1];
			if((!CONSTANTE_EXTENSION_BAJAS.equals(tipoExtension))&&(!CONSTANTE_EXTENSION_NOTAS.equals(tipoExtension))&&
			   (!CONSTANTE_EXTENSION_CDP.equals(tipoExtension))&&(!CONSTANTE_EXTENSION_JSON.equals(tipoExtension))&&
			   (!CONSTANTE_EXTENSION_XML.equals(tipoExtension))&&(!CONSTANTE_EXTENSION_COMPL_DETA.equals(tipoExtension))&&
			   (!CONSTANTE_EXTENSION_COMPL_RELA.equals(tipoExtension))&&(!CONSTANTE_EXTENSION_COMPL_ACAB.equals(tipoExtension))&&
			   (!CONSTANTE_EXTENSION_COMPL_ADET.equals(tipoExtension))&&(!CONSTANTE_EXTENSION_COMPL_LEYE.equals(tipoExtension)))
				resultado = false;
		}
				
		/* Validar que el nombre esta bien conformado */
		String[] validacionNombre = nombreArchivo.split("\\-");
		if(resultado){
			/* Debe estar compuesto por 4 tokens */
			if(validacionNombre.length != 4)
				resultado = false;
		}
		
		/* Validar RUC es numerico y 11 digitos */
		if(resultado){
			String expresion = "^[0-9]{11}$";
			Pattern patron = Pattern.compile(expresion);
			Matcher validador = patron.matcher(validacionNombre[0]);
			resultado = validador.matches();
		}
		
		/* Validar extension en el dominio valido */
		if(resultado){
			if((!CONSTANTE_TIPO_DOCUMENTO_NDEBITO.equals(validacionNombre[1]))&&(!CONSTANTE_TIPO_DOCUMENTO_NCREDITO.equals(validacionNombre[1]))&&
			   (!CONSTANTE_TIPO_DOCUMENTO_FACTURA.equals(validacionNombre[1]))&&(!CONSTANTE_TIPO_DOCUMENTO_BOLETA.equals(validacionNombre[1]))&&
			   (!CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(validacionNombre[1])))
				resultado = false;
		}
		
		/* Validar serie sea 4 caracteres */
		if(resultado){
			String expresion = "";
			if(!CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(validacionNombre[1]))
				expresion = "^[a-zA-Z0-9]{4}$";
			else
				expresion = "^[a-zA-Z0-9]{8}$";
			
			Pattern patron = Pattern.compile(expresion);
			Matcher validador = patron.matcher(validacionNombre[2]);
			resultado = validador.matches();
		}		
		
		return resultado;
	}
	
	
}