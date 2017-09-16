package sfs.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import pe.gob.sunat.facturador.ws.client.WebServiceConsultaWrapper;
import pe.gob.sunat.facturaelectronica.service.wrapper.Response;
import pe.gob.sunat.facturaelectronica.service.wrapper.SunatGEMServiceWrapper;
import pe.gob.sunat.facturaelectronica.service.wrapper.UsuarioSol;
import sfs.model.dao.TxxxyDAO;
import sfs.model.dao.TxxxzDAO;
import sfs.model.domain.TxxxyBean;
import sfs.model.domain.TxxxzBean;
import sfs.util.FacturadorUtil;
import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import static sfs.util.Constantes.CONSTANTE_ALMCERT;
import static sfs.util.Constantes.CONSTANTE_DATA;
import static sfs.util.Constantes.CONSTANTE_ENVIO;
import static sfs.util.Constantes.CONSTANTE_FIRMA;
import static sfs.util.Constantes.CONSTANTE_PARSE;
import static sfs.util.Constantes.CONSTANTE_FORMATO;
import static sfs.util.Constantes.CONSTANTE_RPTA;
import static sfs.util.Constantes.CONSTANTE_TEMP;
import static sfs.util.Constantes.CONSTANTE_SITUACION_ENVIADO_ACEPTADO;
import static sfs.util.Constantes.CONSTANTE_SITUACION_ENVIADO_ACEPTADO_OBSERVACIONES;
import static sfs.util.Constantes.CONSTANTE_SITUACION_ENVIADO_RECHAZADO;
import static sfs.util.Constantes.CONSTANTE_SITUACION_ENVIADO_ANULADO;
import static sfs.util.Constantes.CONSTANTE_SITUACION_CON_ERRORES;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_RBAJAS;
import static sfs.util.Constantes.PRIVATE_KEY_ALIAS;
import static sfs.util.Constantes.KEYSTORE_TYPE;
import static sfs.util.Constantes.KEYSTORE_PASSWORD;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_NDEBITO;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_NCREDITO;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_FACTURA;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_BOLETA;
import static sfs.util.Constantes.CONSTANTE_CODIGO_MONTO_DSCTO;
import static sfs.util.Constantes.CONSTANTE_CODIGO_OPER_EXONERADA;
import static sfs.util.Constantes.CONSTANTE_CODIGO_OPER_INAFECTA;
import static sfs.util.Constantes.CONSTANTE_CODIGO_OPER_GRAVADA;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCU_EMISOR;
import static sfs.util.Constantes.CONSTANTE_UBL_VERSION;
import static sfs.util.Constantes.CONSTANTE_CUSTOMIZATION_ID;
import static sfs.util.Constantes.CONSTANTE_CODIGO_PAIS;
import static sfs.util.Constantes.CONSTANTE_ID_IVG;
import static sfs.util.Constantes.CONSTANTE_COD_IVG;
import static sfs.util.Constantes.CONSTANTE_COD_EXT_IVG;
import static sfs.util.Constantes.CONSTANTE_ID_ISC;
import static sfs.util.Constantes.CONSTANTE_COD_ISC;
import static sfs.util.Constantes.CONSTANTE_COD_EXT_ISC;
import static sfs.util.Constantes.CONSTANTE_ID_OTR;
import static sfs.util.Constantes.CONSTANTE_COD_OTR;
import static sfs.util.Constantes.CONSTANTE_COD_EXT_OTR;
import static sfs.util.Constantes.CONSTANTE_ID_PER;
import static sfs.util.Constantes.CONSTANTE_TIPO_CODIGO_MONEDA_ONEROSO;
import static sfs.util.Constantes.CONSTANTE_TIPO_CODIGO_MONEDA_GRATUITO;
import static sfs.util.Constantes.CONSTANTE_TIPO_CODIGO_PLACA;
import static sfs.util.Constantes.CONSTANTE_CODIGO_OPER_GRATUITA;
import static sfs.util.Constantes.CONSTANTE_SITUACION_ENVIADO_POR_PROCESAR;
import static sfs.util.Constantes.CONSTANTE_COD_MONEDA_SOLES;
import static sfs.util.Constantes.CONSTANTE_INFO_SFS_SUNAT;
import static sfs.util.Constantes.CONSTANTE_VERSION_SFS;
import static sfs.util.Constantes.CONSTANTE_CODIGO_ENVIO_PREVIO;
import static sfs.util.Constantes.CONSTANTE_CODIGO_EXITO_CONSULTA_CDR;

@Service
public class GenerarDocumentosServiceImpl implements GenerarDocumentosService {
	
	private static final Log log = LogFactory.getLog(GenerarDocumentosServiceImpl.class);
	
	@Autowired
	private TxxxyDAO txxxyDAO;
	
	@Autowired
	private TxxxzDAO txxxzDAO;
	
	@Autowired
	private ComunesService comunesService;
	
	@Override
	public String validaCertificado(String input, String numRuc, String passPrivateKey) throws Exception {
		String validacion = "", alias = "",cadena="";
		String cadenaValidar = numRuc;
		Boolean certificadoCorrecto = false;
		Certificate cf = null;
		FileInputStream fis = new FileInputStream(input);
		KeyStore ks = KeyStore.getInstance("PKCS12"); 
		ks.load(fis,passPrivateKey.toCharArray());
		for (Enumeration<String> e=ks.aliases(); e.hasMoreElements(); ) {
			if(!certificadoCorrecto){
				alias = (String)e.nextElement();
				if (ks.isKeyEntry(alias)){
					cf = ks.getCertificate(alias);
					cadena = cf.toString();
	
					if(cadena.indexOf(cadenaValidar) > 0 )
						certificadoCorrecto = true;
				}
			}else
				break;
		}
	 		  
		/*
	  	String cadenaValidar = numRuc;
	  	String cadena = cf.toString();
		 */
	 			  
		/*if(cadena.indexOf(cadenaValidar) == -1 )*/
		if(!certificadoCorrecto)
			validacion = "El propietario del certificado no es el RUC " + numRuc;
		else 
			validacion = "[ALIAS]:" + alias;
   
		return validacion;
	}
	
	@Override	
	public void validarSchemaXML(String tipoComprobante, String nombreArchivo) throws Exception{
		
		try{
			
			String schemaValidador = "";
			
			if(CONSTANTE_TIPO_DOCUMENTO_FACTURA.equals(tipoComprobante)||CONSTANTE_TIPO_DOCUMENTO_BOLETA.equals(tipoComprobante))
				schemaValidador = "schemas/UBLPE-Invoice-1.0.xsd";
			if(CONSTANTE_TIPO_DOCUMENTO_NCREDITO.equals(tipoComprobante))
				schemaValidador = "schemas/UBLPE-CreditNote-1.0.xsd";
			if(CONSTANTE_TIPO_DOCUMENTO_NDEBITO.equals(tipoComprobante))
				schemaValidador = "schemas/UBLPE-DebitNote-1.0.xsd";
			if(CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(tipoComprobante))
				schemaValidador = "schemas/UBLPE-VoidedDocuments-1.0.xsd";

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		    builderFactory.setNamespaceAware(true);
	
		    DocumentBuilder parser = builderFactory.newDocumentBuilder();
	
		    File file = new File(nombreArchivo);
		    InputStream inputStream= new FileInputStream(file);
			Reader reader = new InputStreamReader(inputStream,"ISO8859_1");
			
			Document document = parser.parse(new InputSource(reader));
	   
		    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		    factory.setResourceResolver(new ResourceResolver());
	
		    Source schemaFile = new StreamSource(getClass().getResourceAsStream(schemaValidador));
		    Schema schema = factory.newSchema(schemaFile);
	
		    Validator validator = schema.newValidator();
		    validator.validate(new DOMSource(document));
		    
		}catch(Exception e){
			log.error("Error al Validar Schema: " + e.getMessage());
			throw new Exception("No se puede leer (parsear) el archivo XML: " + e.getMessage() + " - Causa: " + e.getCause() );
			
		}

		
	}
	
	@Override	
	public String validarXML(String tipoComprobante, String rutaEntrada, String nombreArchivo) throws Exception
	{
		String inputXSL = "",  outputHTML = "", retorno = "";
		
		try{
			
			String dataXML = rutaEntrada+nombreArchivo+".xml";
			
			if(CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(tipoComprobante))
				inputXSL = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO)+"ValidaRBajas.xsl";
			if (CONSTANTE_TIPO_DOCUMENTO_BOLETA.equals(tipoComprobante))
				inputXSL = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO)+"ValidaBoleta.xsl";
			if(CONSTANTE_TIPO_DOCUMENTO_FACTURA.equals(tipoComprobante))
				inputXSL = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO)+"ValidaFactura.xsl";
			if(CONSTANTE_TIPO_DOCUMENTO_NCREDITO.equals(tipoComprobante))
				inputXSL = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO)+"ValidaNotaCredito.xsl";
			if(CONSTANTE_TIPO_DOCUMENTO_NDEBITO.equals(tipoComprobante))
				inputXSL = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO)+"ValidaNotaDebito.xsl";
			
			outputHTML = comunesService.obtenerRutaTrabajo(CONSTANTE_PARSE)+nombreArchivo+".xml";
			retorno = this.transform(dataXML, inputXSL, outputHTML);

			if(!"".equals(retorno))
				throw new Exception("Error al validar XML: " + retorno);
			
			
		}catch(Exception e){
			
			log.error(e.getMessage());
			throw new Exception(e.getMessage());
			
		}
					
		return  retorno;

	}
	
	@Override
	public void formatoPlantillaXml(String tipoDocumento, String[] archivos, String nombreArchivo) throws Exception{
	  Map<String,Object> root = null;
      String plantillaSeleccionada = "";
      
      // Formato de Resumen de Bajas
      if (CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(tipoDocumento)){
		  root = formatoResumenBajas(archivos[0],nombreArchivo);
		  plantillaSeleccionada = "ConvertirRBajasXML.ftl";
   	  }
	  
      // Formato de Facturas
      if (CONSTANTE_TIPO_DOCUMENTO_FACTURA.equals(tipoDocumento)||CONSTANTE_TIPO_DOCUMENTO_BOLETA.equals(tipoDocumento)){
		  root = formatoFactura(archivos[0],archivos[1],archivos[2],archivos[3],archivos[4],archivos[5], nombreArchivo);
		  plantillaSeleccionada = "ConvertirFacturaXML.ftl";
      }
      
   	  if (CONSTANTE_TIPO_DOCUMENTO_NCREDITO.equals(tipoDocumento)){
		  root = formatoNotaCredito(archivos[0],archivos[1],archivos[2],archivos[3],archivos[4],archivos[5], nombreArchivo);
		  plantillaSeleccionada = "ConvertirNCreditoXML.ftl";
   	  }
	  if (CONSTANTE_TIPO_DOCUMENTO_NDEBITO.equals(tipoDocumento)){
		  root = formatoNotaDebito(archivos[0],archivos[1],archivos[2],archivos[3],archivos[4],archivos[5], nombreArchivo);
		  plantillaSeleccionada = "ConvertirNDebitoXML.ftl";
	  }
	  
	  
	  File archivoFTL = new File(comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO), plantillaSeleccionada);
		
	  if(!archivoFTL.exists())
		throw new Exception("No existe la plantilla para el tipo documento a generar XML (Archivo FTL).");
	  
  
      Configuration cfg = new Configuration();
      cfg.setDirectoryForTemplateLoading(new File(comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO)));
      cfg.setDefaultEncoding("ISO8859_1");
      cfg.setLocale(Locale.US);
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
     
      Template temp = cfg.getTemplate(plantillaSeleccionada);
      StringBuilder rutaSalida = new StringBuilder(); 
      rutaSalida.setLength(0);
      rutaSalida.append(comunesService.obtenerRutaTrabajo(CONSTANTE_TEMP))
                .append(nombreArchivo)
                .append(".xml");
      OutputStream outputStream = new FileOutputStream(rutaSalida.toString());
      Writer out = new OutputStreamWriter(outputStream);
      temp.process(root, out);   
      outputStream.close();        
		
	}
	
	private Map<String,Object> formatoResumenBajas(String archivoCabecera, String nombreArchivo) throws Exception{
		String numRuc = "", razonSocial = "", param ="", cadena ="",nombreComercial="";
		String[] registro;
		Integer error = new Integer(0);
		/* Cargando Parametros del Facturador */
		TxxxyBean paramtroBean = new TxxxyBean();
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
		if(listaParametros.size() > 0){
			for(TxxxyBean parametro : listaParametros ){
				param = parametro.getVal_para();
				if("NOMCOM".equals(parametro.getCod_para())) nombreComercial = param;
				if("NUMRUC".equals(parametro.getCod_para())) numRuc = param;
				if("RAZON".equals(parametro.getCod_para())) razonSocial = param;
			}
		}
		
		/* Leyendo Archivo de Cabecera de resumen de bajas */
		Map<String,Object> resumenGeneral =  new HashMap<String,Object>();
		Map<String,Object> resumenBajas = null;
		List<Map<String,Object>> listaResumenBajas = new ArrayList<Map<String,Object>>();
		FileReader fArchivoCabecera = new FileReader(archivoCabecera);
		BufferedReader bArchivoCabereca = new BufferedReader(fArchivoCabecera);
		Integer linea = 0;
		while((cadena = bArchivoCabereca.readLine())!=null) {
			registro = cadena.split("\\|");
		
		if(registro.length != 5 && error == 0){
			error = new Integer(1);
			bArchivoCabereca.close();
			throw new Exception("El archivo CBA no contiene la cantidad de columnas esperada (5 columnas).");
		}else{
			// Linea de Comunicacion
			linea ++;
			
			// Generando ID de Comunicacion
			String idArchivo[] = nombreArchivo.split("\\-");
			String idComunicacion = idArchivo[1]+"-"+idArchivo[2]+"-"+idArchivo[3];
			
			// Desde Archivo Txt
			String fechaDocumentoBaja = registro[0];
			String fechaComunicacionBaja = registro[1];
			String tipoDocumento = registro[2];
			String serieNumeroDocumento = registro[3];
			String motivoBajaDocumento = registro[4];
			String nroDocumento[] = serieNumeroDocumento.split("\\-");
			String identificadorFirmaSwf = "SIGN";
			Random calcularRnd = new Random(); 
			Integer codigoFacturadorSwf = (int)(calcularRnd.nextDouble() * 1000000); 
			
			if(linea == 1){
				resumenGeneral.put("nombreComercialSwf", nombreComercial);
				resumenGeneral.put("razonSocialSwf", razonSocial);
				resumenGeneral.put("nroRucEmisorSwf", numRuc);
				resumenGeneral.put("tipDocuEmisorSwf", CONSTANTE_TIPO_DOCU_EMISOR);
				resumenGeneral.put("fechaDocumentoBaja", fechaDocumentoBaja);
				resumenGeneral.put("fechaComunicacioBaja", fechaComunicacionBaja);
				// Valores Automaticos
				resumenGeneral.put("ublVersionIdSwf", CONSTANTE_UBL_VERSION);
				resumenGeneral.put("idComunicacion", idComunicacion);
				resumenGeneral.put("CustomizationIdSwf", CONSTANTE_CUSTOMIZATION_ID);
				resumenGeneral.put("identificadorFacturadorSwf",CONSTANTE_INFO_SFS_SUNAT + CONSTANTE_VERSION_SFS);
				resumenGeneral.put("codigoFacturadorSwf",codigoFacturadorSwf.toString());
				resumenGeneral.put("identificadorFirmaSwf",identificadorFirmaSwf);
				
				resumenBajas = new HashMap<String,Object>();
				resumenBajas.put("tipoDocumentoBaja", tipoDocumento);
				resumenBajas.put("serieDocumentoBaja", nroDocumento[0]);
				resumenBajas.put("nroDocumentoBaja", nroDocumento[1]);
				resumenBajas.put("motivoBajaDocumento", motivoBajaDocumento);
				resumenBajas.put("linea",linea);

				
			}else{
				
				resumenBajas = new HashMap<String,Object>();
				resumenBajas.put("tipoDocumentoBaja", tipoDocumento);
				resumenBajas.put("serieDocumentoBaja", nroDocumento[0]);
				resumenBajas.put("nroDocumentoBaja", nroDocumento[1]);
				resumenBajas.put("motivoBajaDocumento", motivoBajaDocumento);
				resumenBajas.put("linea",linea);
			}
		
			listaResumenBajas.add(resumenBajas);
						
		  	}
		
		  }
		  bArchivoCabereca.close();
		  
		  resumenGeneral.put("listaResumen", listaResumenBajas); 
      
		  return resumenGeneral;
	}
	
	@SuppressWarnings("unchecked")
	private void formatoComunes(Map<String,Object> comprobante, String archivoRelacionado, String archivoAdiCabecera, String archivoAdiDetalle, String archivoLeyendas) throws Exception{
		String cadena ="";
		Integer activarRelacionado = 0, activarAdiCabecera = 0, activarAdiDetalle=0, activarLeyendas = 0;
		String[] registro;
		
		/* Validar que Archivos serán procesados */
		File fileArchivoRelacionado = new File(archivoRelacionado);
		if (fileArchivoRelacionado.exists()) 
			activarRelacionado = 1;
		
		File fileArchivoAdiCabecera = new File(archivoAdiCabecera);
		if (fileArchivoAdiCabecera.exists()) 
			activarAdiCabecera = 1;
		
		File fileArchivoAdiDetalle = new File(archivoAdiDetalle);
		if (fileArchivoAdiDetalle.exists()) 
			activarAdiDetalle = 1;
		
		File fileArchivoLeyendas = new File(archivoLeyendas);
		if (fileArchivoLeyendas.exists()) 
			activarLeyendas = 1;
		
		/* Leyendo Archivo de Adicionales Cabecera del Comprobante */
		if(activarAdiCabecera == 1){
			FileReader fArchivoAdiCabecera = new FileReader(archivoAdiCabecera);
			BufferedReader bArchivoAdiCabecera = new BufferedReader(fArchivoAdiCabecera);
			if((cadena = bArchivoAdiCabecera.readLine())!=null){
	          	registro = cadena.split("\\|");

	          	if(registro.length != 13){
	          		bArchivoAdiCabecera.close();
	          		throw new Exception("El archivo adicionales cabecera no continene la cantidad de datos esperada (13 columnas).");
	          	}else{
	          		comprobante.put("codRegiPercepcion",registro[0]);
	          		comprobante.put("baseImponiblePercepcion",registro[1]);
	          		comprobante.put("montoPercepcion",registro[2]);
	          		comprobante.put("montoTotalSumPercepcion",registro[3]);
	          		comprobante.put("totalVentaOperGratuita",registro[4]);
	          		comprobante.put("totalAnticipos",registro[5]);
	          		comprobante.put("codigoPaisCliente",registro[6]);
	          		comprobante.put("codigoUbigeoCliente",registro[7]);
	          		comprobante.put("direccionCliente",registro[8]);
	          		comprobante.put("codigoPaisEntrega",registro[9]);
	          		comprobante.put("codigoUbigeoEntrega",registro[10]);
	          		comprobante.put("direccionCompletaEntrega",registro[11]);
	          		comprobante.put("fechaVencimiento",registro[12]);
	          		comprobante.put("codigoPercepSwf",CONSTANTE_ID_PER);
	          		comprobante.put("codigoGratuitoSwf",CONSTANTE_CODIGO_OPER_GRATUITA);
	          		comprobante.put("codigoMonedaSolesSwf",CONSTANTE_COD_MONEDA_SOLES);
	          		
	          	}
	          }
			  bArchivoAdiCabecera.close();
		   }
		
		
		  if(activarAdiDetalle == 1){
			  FileReader fArchivoAdiDetalle = new FileReader(archivoAdiDetalle);
			  BufferedReader bArchivoAdiDetalle = new BufferedReader(fArchivoAdiDetalle);
	          			
			  /* Leyendo Archivo de Adicionales al Detalle del Comprobante */
			  List<Map<String,Object>> listaDetalle = (ArrayList<Map<String,Object>>)comprobante.get("listaDetalle");
			  for(Map<String, Object> entry: listaDetalle){
				  
				  if((cadena = bArchivoAdiDetalle.readLine())!=null) {
					  if(!"".equals(cadena.trim())){
					  	registro = cadena.split("\\|");
	                  	if(registro.length != 2){
	                  		bArchivoAdiDetalle.close();
	                  		throw new Exception("El archivo adicionales detalle no continene la cantidad de datos esperada (2 columnas).");
	                  	}else{
	                  		entry.put("monto",registro[0]);
	                  		entry.put("placa",registro[1]);
	                  		entry.put("tipoCodigoMonedaSwf",CONSTANTE_TIPO_CODIGO_MONEDA_ONEROSO);
	                  		entry.put("tipoCodigoPlacaSwf",CONSTANTE_TIPO_CODIGO_PLACA);
	                  	}
					  }
	              }
			  }
			  comprobante.put("listaDetalle", listaDetalle);
			  bArchivoAdiDetalle.close();
		  }
				
		  /* Leyendo Archivo de Relacionados del Comprobante */
	      List<Map<String,Object>> listaRelacionado = new ArrayList<Map<String,Object>>(); 
	      Map<String,Object> relacionado = null;
	      if(activarRelacionado == 1){
	    	  
	    	  FileReader fArchivoRelacionado = new FileReader(archivoRelacionado);
	          BufferedReader bArchivoRelacionado = new BufferedReader(fArchivoRelacionado);
	          while((cadena = bArchivoRelacionado.readLine())!=null) {
	          	registro = cadena.split("\\|");
	          	
	          	if(registro.length != 6){
	          		bArchivoRelacionado.close();
	    			throw new Exception("El archivo documento relacionado no continene la cantidad de datos esperada (6 columnas).");
	          	}else{
	          		relacionado = new HashMap<String,Object>();
	          		relacionado.put("indDocuRelacionado",registro[0]);
	          		relacionado.put("tipDocuRelacionado",registro[1]);
	          		relacionado.put("nroDocuRelacionado",registro[2]);
	          		relacionado.put("tipDocuEmisor",registro[3]);
	          		relacionado.put("nroDocuEmisor",registro[4]);
	          		relacionado.put("mtoDocuRelacionado",registro[5]);
	
	          		listaRelacionado.add(relacionado);	
	    			
	          	}
	          }
	          bArchivoRelacionado.close();
	          comprobante.put("listaRelacionado", listaRelacionado);
	    	  
	      }else{
	    	  
	    	  comprobante.put("listaRelacionado", listaRelacionado);
	      }
	           
	      
	      /* Leyendo Archivo de Leyendas del Comprobante */
	      List<Map<String,Object>> listaLeyendas = new ArrayList<Map<String,Object>>(); 
	      Map<String,Object> leyendas = null;

	      if(activarLeyendas == 1){
	    	  
	    	  FileReader fArchivoLeyendas = new FileReader(archivoLeyendas);
	          BufferedReader bArchivoLeyendas = new BufferedReader(fArchivoLeyendas);
	          while((cadena = bArchivoLeyendas.readLine())!=null) {
	          	registro = cadena.split("\\|");
	          	
	          	if(registro.length != 2){
	          		bArchivoLeyendas.close();
	          		throw new Exception("El archivo de leyendas no continene la cantidad de datos esperada (2 columnas).");
	          	}else{
	          		leyendas = new HashMap<String,Object>();
	          		leyendas.put("codigo",registro[0]);
	          		leyendas.put("descripcion",registro[1]);
	         		
	          		listaLeyendas.add(leyendas);
	          	}
	          }
	          bArchivoLeyendas.close();
	          comprobante.put("listaLeyendas", listaLeyendas);
	   	  
	      }else{
	    	  
	    	  comprobante.put("listaLeyendas", listaLeyendas);
	      }
		
	}
	
	
	private Map<String,Object> formatoFactura( String archivoCabecera, String archivoDetalle, String archivoRelacionado, String archivoAdiCabecera, String archivoAdiDetalle, String archivoLeyendas, String nombreArchivo) throws Exception{
		String cadena ="", nombreComercial="",ubigeo="", direccion="", param="",numRuc="",razonSocial="";
		Integer activarCabecera = 0, activarDetalle = 0;
		String[] registro;
		/* Cargando PArametros de Firma */
		String identificadorFirmaSwf = "SIGN";
		Random calcularRnd = new Random(); 
		Integer codigoFacturadorSwf = (int)(calcularRnd.nextDouble() * 1000000); 
		
		/* Cargando Parametros del Facturador */
		TxxxyBean paramtroBean = new TxxxyBean();
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
		if(listaParametros.size() > 0){
			for(TxxxyBean parametro : listaParametros ){
				param = parametro.getVal_para();
				if("NUMRUC".equals(parametro.getCod_para())) numRuc = param;
				if("RAZON".equals(parametro.getCod_para())) razonSocial = param;
				if("NOMCOM".equals(parametro.getCod_para())) nombreComercial = param;
				if("UBIGEO".equals(parametro.getCod_para())) ubigeo = param;
				if("DIRECC".equals(parametro.getCod_para())) direccion = param;
			}
		}
		
		/* Validar que Archivos serán procesados */
		File fileArchivoCabecera = new File(archivoCabecera);
		if (fileArchivoCabecera.exists()) 
			activarCabecera = 1;
		
		File fileArchivoDetalle = new File(archivoDetalle);
		if (fileArchivoDetalle.exists()) 
			activarDetalle = 1;
		
		/* Obtener Datos de Archivo */
		String datosArchivo[]  = nombreArchivo.split("\\-");
		
		/* Leyendo Archivo de Cabecera del Comprobante */	
		Map<String,Object> factura = null;
		if(activarCabecera==1){
			
			FileReader fArchivoCabecera = new FileReader(archivoCabecera);
			BufferedReader bArchivoCabereca = new BufferedReader(fArchivoCabecera);
			
			while((cadena = bArchivoCabereca.readLine())!=null) {
				registro = cadena.split("\\|");
			
			if(registro.length != 17){
				bArchivoCabereca.close();
				throw new Exception("El archivo cabecera no continene la cantidad de datos esperada (17 columnas).");
			}else{
				// Desde Archivo Txt
				factura = new HashMap<String,Object>();
				factura.put("tipoOperacion", registro[0]); // NUEVO
				factura.put("fechaEmision", registro[1]);
				factura.put("direccionUsuario", registro[2]); // NUEVO
				factura.put("tipoDocumento", registro[3]);
				factura.put("nroDocumento", registro[4]);
				factura.put("razonSocialUsuario", registro[5]);
				factura.put("moneda", registro[6]);
				factura.put("descuentoGlobal", registro[7]); // CAMBIO
				factura.put("sumaOtrosCargos", registro[8]);
				factura.put("totalDescuento", registro[9]);  // CAMBIO
				factura.put("montoOperGravadas", registro[10]);
				factura.put("montoOperInafectas", registro[11]);
				factura.put("montoOperExoneradas", registro[12]);
				factura.put("sumaIgv", registro[13]);
				factura.put("sumaIsc", registro[14]);
				factura.put("sumaOtros", registro[15]);
				factura.put("sumaImporteVenta", registro[16]);
							
				// Valores Automaticos
				factura.put("ublVersionIdSwf", CONSTANTE_UBL_VERSION);
				factura.put("CustomizationIdSwf", CONSTANTE_CUSTOMIZATION_ID);
				factura.put("nroCdpSwf", datosArchivo[2]+"-"+datosArchivo[3]); // NUEVO
				factura.put("tipCdpSwf", datosArchivo[1]); // NUEVO
				factura.put("nroRucEmisorSwf", numRuc);
				factura.put("tipDocuEmisorSwf", CONSTANTE_TIPO_DOCU_EMISOR);
				factura.put("nombreComercialSwf", nombreComercial);
				factura.put("razonSocialSwf", razonSocial);
				factura.put("ubigeoDomFiscalSwf", ubigeo);
				factura.put("direccionDomFiscalSwf", direccion);
				factura.put("paisDomFiscalSwf", CONSTANTE_CODIGO_PAIS);
				factura.put("codigoMontoDescuentosSwf", CONSTANTE_CODIGO_MONTO_DSCTO);
				factura.put("codigoMontoOperGravadasSwf", CONSTANTE_CODIGO_OPER_GRAVADA);
				factura.put("codigoMontoOperInafectasSwf", CONSTANTE_CODIGO_OPER_INAFECTA);
				factura.put("codigoMontoOperExoneradasSwf", CONSTANTE_CODIGO_OPER_EXONERADA);
				factura.put("idIgv", CONSTANTE_ID_IVG);
				factura.put("codIgv", CONSTANTE_COD_IVG);
				factura.put("codExtIgv", CONSTANTE_COD_EXT_IVG);
				factura.put("idIsc", CONSTANTE_ID_ISC);
				factura.put("codIsc", CONSTANTE_COD_ISC);
				factura.put("codExtIsc", CONSTANTE_COD_EXT_ISC);
				factura.put("idOtr", CONSTANTE_ID_OTR);
				factura.put("codOtr", CONSTANTE_COD_OTR);
				factura.put("codExtOtr", CONSTANTE_COD_EXT_OTR);
				factura.put("tipoCodigoMonedaSwf",CONSTANTE_TIPO_CODIGO_MONEDA_ONEROSO);
				factura.put("identificadorFacturadorSwf",CONSTANTE_INFO_SFS_SUNAT + CONSTANTE_VERSION_SFS);
				factura.put("codigoFacturadorSwf",codigoFacturadorSwf.toString());
				factura.put("identificadorFirmaSwf",identificadorFirmaSwf);

			  	}
			  }
			  bArchivoCabereca.close();
			
		}else{
			
			throw new Exception("No Existe el formato de la cabecera del Comprobante de Pago."); 
			
		}
		
      
      /* Leyendo Archivo de Detalle del Comprobante */
      List<Map<String,Object>> listaDetalle = new ArrayList<Map<String,Object>>(); 
      Map<String,Object> detalle = null;
            
      if(activarDetalle == 1){
    	  Integer linea = 0;
          FileReader fArchivoDetalle = new FileReader(archivoDetalle);
          BufferedReader bArchivoDetalle = new BufferedReader(fArchivoDetalle);
          
          /* Abrir Archivo de Detalle en caso exista*/
          while((cadena = bArchivoDetalle.readLine())!=null) {
          	registro = cadena.split("\\|");
          	if(registro.length != 13){
          		bArchivoDetalle.close();
    			throw new Exception("El archivo detalle no continene la cantidad de datos esperada (13 columnas).");
          	}else{
          		// Desde Archivo Txt
          		linea = linea + 1;
          		detalle = new HashMap<String,Object>();
          		detalle.put("unidadMedida", registro[0]);
          		detalle.put("cantItem", registro[1]);
          		detalle.put("codiProducto", registro[2]);
          		detalle.put("codiSunat", registro[3]);
          		detalle.put("desItem", registro[4]);
          		detalle.put("valorUnitario", registro[5]);
          		detalle.put("descuentoItem", registro[6]);
          		detalle.put("montoIgvItem", registro[7]);
          		detalle.put("afectaIgvItem", registro[8]);
          		detalle.put("montoIscItem", registro[9]);
          		detalle.put("tipoSistemaIsc", registro[10]);
          		detalle.put("precioVentaUnitarioItem", registro[11]);
          		detalle.put("valorVentaItem", registro[12]);
          		// Valores Automaticos
          		detalle.put("lineaSwf", linea);
          		detalle.put("tipoCodiMoneGratiSwf",CONSTANTE_TIPO_CODIGO_MONEDA_GRATUITO);
          		
          		listaDetalle.add(detalle);
          	}
          }
       	  bArchivoDetalle.close();
          factura.put("listaDetalle", listaDetalle);
          
          /* Agregar informacion adicional */
          formatoComunes(factura, archivoRelacionado, archivoAdiCabecera, archivoAdiDetalle, archivoLeyendas);
          

      }else{
			
			throw new Exception("No Existe el formato del detalle del Comprobante de Pago."); 
			
      }
      
      return factura;
      
	}
		
	private Map<String,Object> formatoNotaCredito(String archivoCabecera, String archivoDetalle, String archivoRelacionado, String archivoAdiCabecera, String archivoAdiDetalle, String archivoLeyendas, String nombreArchivo) throws Exception{
		String cadena ="", nombreComercial="",ubigeo="", direccion="",  param="",numRuc="",razonSocial="";
		String[] registro;
		
		/* Cargando PArametros de Firma */
		String identificadorFirmaSwf = "SIGN";
		Random calcularRnd = new Random(); 
		Integer codigoFacturadorSwf = (int)(calcularRnd.nextDouble() * 1000000); 
		
		/* Obtener Datos de Archivo */
		String datosArchivo[]  = nombreArchivo.split("\\-");
		
		/* Cargando Parametros del Facturador */
		TxxxyBean paramtroBean = new TxxxyBean();
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
		if(listaParametros.size() > 0){
			for(TxxxyBean parametro : listaParametros ){
				param = parametro.getVal_para();
				if("NUMRUC".equals(parametro.getCod_para())) numRuc = param;
				if("RAZON".equals(parametro.getCod_para())) razonSocial = param;
				if("NOMCOM".equals(parametro.getCod_para())) nombreComercial = param;
				if("UBIGEO".equals(parametro.getCod_para())) ubigeo = param;
				if("DIRECC".equals(parametro.getCod_para())) direccion = param;
			}
		}
		
		/* Leyendo Archivo de Cabecera de nota de credito */	
		Map<String,Object> notaCredito = null;
		FileReader fArchivoCabecera = new FileReader(archivoCabecera);
		BufferedReader bArchivoCabereca = new BufferedReader(fArchivoCabecera);
		while((cadena = bArchivoCabereca.readLine())!=null) {
			registro = cadena.split("\\|");
			if(registro.length != 17) {
				bArchivoCabereca.close();
				throw new Exception("El archivo cabecera no continene la cantidad de datos esperada (17 columnas).");
			}else{
				// Desde Archivo Txt
				notaCredito = new HashMap<String,Object>();
				notaCredito.put("fechaEmision", registro[0]);
				notaCredito.put("codigoMotivo", registro[1]);
				notaCredito.put("descripcionMotivo", registro[2]);
				notaCredito.put("tipoDocuModifica", registro[3]);
				notaCredito.put("nroDocuModifica", registro[4]); // NUEVO
				notaCredito.put("tipoDocuIdenti", registro[5]);
				notaCredito.put("nroDocuIdenti", registro[6]);
				notaCredito.put("razonSocialUsuario", registro[7]);
				notaCredito.put("moneda", registro[8]);
				notaCredito.put("sumaOtrosCargos", registro[9]);
				notaCredito.put("montoOperGravadas", registro[10]);
				notaCredito.put("montoOperInafectas", registro[11]);
				notaCredito.put("montoOperExoneradas", registro[12]);
				notaCredito.put("sumaIgv", registro[13]);
				notaCredito.put("sumaIsc", registro[14]);
				notaCredito.put("sumaOtros", registro[15]);
				notaCredito.put("sumaImporteVenta", registro[16]);
				
				
				// Valores Automaticos
				notaCredito.put("ublVersionIdSwf", CONSTANTE_UBL_VERSION);
				notaCredito.put("CustomizationIdSwf", CONSTANTE_CUSTOMIZATION_ID);
				notaCredito.put("nroCdpSwf", datosArchivo[2]+"-"+datosArchivo[3]); // NUEVO
				notaCredito.put("tipCdpSwf", datosArchivo[1]); // NUEVO
				notaCredito.put("nroRucEmisorSwf", numRuc);
				notaCredito.put("tipDocuEmisorSwf", CONSTANTE_TIPO_DOCU_EMISOR);
				notaCredito.put("nombreComercialSwf", nombreComercial);
				notaCredito.put("razonSocialSwf", razonSocial);
				notaCredito.put("ubigeoDomFiscalSwf", ubigeo);
				notaCredito.put("direccionDomFiscalSwf", direccion);
				notaCredito.put("paisDomFiscalSwf", CONSTANTE_CODIGO_PAIS);
				
				notaCredito.put("codigoMontoDescuentosSwf", CONSTANTE_CODIGO_MONTO_DSCTO);
				notaCredito.put("codigoMontoOperGravadasSwf", CONSTANTE_CODIGO_OPER_GRAVADA);
				notaCredito.put("codigoMontoOperInafectasSwf", CONSTANTE_CODIGO_OPER_INAFECTA);
				notaCredito.put("codigoMontoOperExoneradasSwf", CONSTANTE_CODIGO_OPER_EXONERADA);
				notaCredito.put("idIgv", CONSTANTE_ID_IVG);
				notaCredito.put("codIgv", CONSTANTE_COD_IVG);
				notaCredito.put("codExtIgv", CONSTANTE_COD_EXT_IVG);
				notaCredito.put("idIsc", CONSTANTE_ID_ISC);
				notaCredito.put("codIsc", CONSTANTE_COD_ISC);
				notaCredito.put("codExtIsc", CONSTANTE_COD_EXT_ISC);
				notaCredito.put("idOtr", CONSTANTE_ID_OTR);
				notaCredito.put("codOtr", CONSTANTE_COD_OTR);
				notaCredito.put("codExtOtr", CONSTANTE_COD_EXT_OTR);
				notaCredito.put("tipoCodigoMonedaSwf",CONSTANTE_TIPO_CODIGO_MONEDA_ONEROSO);
				

				notaCredito.put("identificadorFacturadorSwf",CONSTANTE_INFO_SFS_SUNAT + CONSTANTE_VERSION_SFS);
				notaCredito.put("codigoFacturadorSwf",codigoFacturadorSwf.toString());
				notaCredito.put("identificadorFirmaSwf",identificadorFirmaSwf);
			  }
		}
		bArchivoCabereca.close();

      
      /* Leyendo Archivo de Detalle del nota de credito */
		List<Map<String,Object>> listaDetalle = new ArrayList<Map<String,Object>>(); 
	      Map<String,Object> detalle = null;
	      Integer linea = 0;
	      FileReader fArchivoDetalle = new FileReader(archivoDetalle);
	      BufferedReader bArchivoDetalle = new BufferedReader(fArchivoDetalle);
	      while((cadena = bArchivoDetalle.readLine())!=null) {
	      	registro = cadena.split("\\|");
	      	
	      	if(registro.length != 13){
	      		bArchivoDetalle.close();
				throw new Exception("El archivo detalle no continene la cantidad de datos esperada (13 columnas).");
	      	}else{
	      		// Desde Archivo Txt
	      		linea = linea + 1;
	      		detalle = new HashMap<String,Object>();
	      		detalle.put("unidadMedida", registro[0]);
	      		detalle.put("cantItem", registro[1]);
	      		detalle.put("codiProducto", registro[2]);
	      		detalle.put("codiSunat", registro[3]);
	      		detalle.put("desItem", registro[4]);
	      		detalle.put("valorUnitario", registro[5]);
	      		detalle.put("descuentoItem", registro[6]);
	      		detalle.put("montoIgvItem", registro[7]);
	      		detalle.put("afectaIgvItem", registro[8]);
	      		detalle.put("montoIscItem", registro[9]);
	      		detalle.put("tipoSistemaIsc", registro[10]);
	      		detalle.put("precioVentaUnitarioItem", registro[11]);
	      		detalle.put("valorVentaItem", registro[12]);
	     		
	      		// Valores Automaticos
	      		detalle.put("lineaSwf", linea);
	      		detalle.put("tipoCodiMoneGratiSwf",CONSTANTE_TIPO_CODIGO_MONEDA_GRATUITO);
	     		
	      		listaDetalle.add(detalle);
	      	}
	      }
	      bArchivoDetalle.close();
	      notaCredito.put("listaDetalle", listaDetalle);
	      
	      /* Agregar informacion adicional */
          formatoComunes(notaCredito, archivoRelacionado, archivoAdiCabecera, archivoAdiDetalle, archivoLeyendas);
      
      return notaCredito;
      
	}
	
	
	private Map<String,Object> formatoNotaDebito(String archivoCabecera, String archivoDetalle, String archivoRelacionado, String archivoAdiCabecera, String archivoAdiDetalle, String archivoLeyendas, String nombreArchivo) throws Exception{
		String cadena ="", nombreComercial="",ubigeo="", direccion="",  param="",numRuc="",razonSocial="";
		String[] registro;
		Integer error = new Integer(0);
		
		/* Cargando PArametros de Firma */
		String identificadorFirmaSwf = "SIGN";
		Random calcularRnd = new Random(); 
		Integer codigoFacturadorSwf = (int)(calcularRnd.nextDouble() * 1000000); 
		
		/* Obtener Datos de Archivo */
		String datosArchivo[]  = nombreArchivo.split("\\-");
		
		/* Cargando Parametros del Facturador */
		TxxxyBean paramtroBean = new TxxxyBean();
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
		if(listaParametros.size() > 0){
			for(TxxxyBean parametro : listaParametros ){
				param = parametro.getVal_para();
				if("NUMRUC".equals(parametro.getCod_para())) numRuc = param;
				if("RAZON".equals(parametro.getCod_para())) razonSocial = param;
				if("NOMCOM".equals(parametro.getCod_para())) nombreComercial = param;
				if("UBIGEO".equals(parametro.getCod_para())) ubigeo = param;
				if("DIRECC".equals(parametro.getCod_para())) direccion = param;
			}
		}
		
		/* Leyendo Archivo de Cabecera de nota de debito */	
		Map<String,Object> notaDebito = null;
		FileReader fArchivoCabecera = new FileReader(archivoCabecera);
		BufferedReader bArchivoCabereca = new BufferedReader(fArchivoCabecera);
		while((cadena = bArchivoCabereca.readLine())!=null) {
			registro = cadena.split("\\|");
			if(registro.length != 17 && error == 0){
				error = new Integer(1);
				bArchivoCabereca.close();
				throw new Exception("El archivo cabecera no continene la cantidad de datos esperada (17 columnas).");
			}else{
				// Desde Archivo Txt
				notaDebito = new HashMap<String,Object>();
				notaDebito.put("fechaEmision", registro[0]);
				notaDebito.put("codigoMotivo", registro[1]);
				notaDebito.put("descripcionMotivo", registro[2]);
				notaDebito.put("tipoDocuModifica", registro[3]);
				notaDebito.put("nroDocuModifica", registro[4]); // NUEVO
				notaDebito.put("tipoDocuIdenti", registro[5]);
				notaDebito.put("nroDocuIdenti", registro[6]);
				notaDebito.put("razonSocialUsuario", registro[7]);
				notaDebito.put("moneda", registro[8]);
				notaDebito.put("sumaOtrosCargos", registro[9]);
				notaDebito.put("montoOperGravadas", registro[10]);
				notaDebito.put("montoOperInafectas", registro[11]);
				notaDebito.put("montoOperExoneradas", registro[12]);
				notaDebito.put("sumaIgv", registro[13]);
				notaDebito.put("sumaIsc", registro[14]);
				notaDebito.put("sumaOtros", registro[15]);
				notaDebito.put("sumaImporteVenta", registro[16]);
				
				
				// Valores Automaticos
				notaDebito.put("ublVersionIdSwf", CONSTANTE_UBL_VERSION);
				notaDebito.put("CustomizationIdSwf", CONSTANTE_CUSTOMIZATION_ID);
				notaDebito.put("nroCdpSwf", datosArchivo[2]+"-"+datosArchivo[3]); // NUEVO
				notaDebito.put("tipCdpSwf", datosArchivo[1]); // NUEVO
				notaDebito.put("nroRucEmisorSwf", numRuc);
				notaDebito.put("tipDocuEmisorSwf", CONSTANTE_TIPO_DOCU_EMISOR);
				notaDebito.put("nombreComercialSwf", nombreComercial);
				notaDebito.put("razonSocialSwf", razonSocial);
				notaDebito.put("ubigeoDomFiscalSwf", ubigeo);
				notaDebito.put("direccionDomFiscalSwf", direccion);
				notaDebito.put("paisDomFiscalSwf", CONSTANTE_CODIGO_PAIS);
				
				notaDebito.put("codigoMontoDescuentosSwf", CONSTANTE_CODIGO_MONTO_DSCTO);
				notaDebito.put("codigoMontoOperGravadasSwf", CONSTANTE_CODIGO_OPER_GRAVADA);
				notaDebito.put("codigoMontoOperInafectasSwf", CONSTANTE_CODIGO_OPER_INAFECTA);
				notaDebito.put("codigoMontoOperExoneradasSwf", CONSTANTE_CODIGO_OPER_EXONERADA);
				notaDebito.put("idIgv", CONSTANTE_ID_IVG);
				notaDebito.put("codIgv", CONSTANTE_COD_IVG);
				notaDebito.put("codExtIgv", CONSTANTE_COD_EXT_IVG);
				notaDebito.put("idIsc", CONSTANTE_ID_ISC);
				notaDebito.put("codIsc", CONSTANTE_COD_ISC);
				notaDebito.put("codExtIsc", CONSTANTE_COD_EXT_ISC);
				notaDebito.put("idOtr", CONSTANTE_ID_OTR);
				notaDebito.put("codOtr", CONSTANTE_COD_OTR);
				notaDebito.put("codExtOtr", CONSTANTE_COD_EXT_OTR);
				notaDebito.put("tipoCodigoMonedaSwf",CONSTANTE_TIPO_CODIGO_MONEDA_ONEROSO);
				
				notaDebito.put("identificadorFacturadorSwf",CONSTANTE_INFO_SFS_SUNAT + CONSTANTE_VERSION_SFS);
				notaDebito.put("codigoFacturadorSwf",codigoFacturadorSwf.toString());
				notaDebito.put("identificadorFirmaSwf",identificadorFirmaSwf);
			  }
		}
		bArchivoCabereca.close();

      
      /* Leyendo Archivo de Detalle del nota de credito */
		List<Map<String,Object>> listaDetalle = new ArrayList<Map<String,Object>>(); 
	      Map<String,Object> detalle = null;
	      Integer linea = 0;
	      FileReader fArchivoDetalle = new FileReader(archivoDetalle);
	      BufferedReader bArchivoDetalle = new BufferedReader(fArchivoDetalle);
	      while((cadena = bArchivoDetalle.readLine())!=null) {
	      	registro = cadena.split("\\|");
	      	
	      	if(registro.length != 13){
	      		error = new Integer(1);
	      		bArchivoDetalle.close();
				throw new Exception("El archivo detalle no continene la cantidad de datos esperada (13 columnas).");
	      	}else{
	      		// Desde Archivo Txt
	      		linea = linea + 1;
	      		detalle = new HashMap<String,Object>();
	      		detalle.put("unidadMedida", registro[0]);
	      		detalle.put("cantItem", registro[1]);
	      		detalle.put("codiProducto", registro[2]);
	      		detalle.put("codiSunat", registro[3]);
	      		detalle.put("desItem", registro[4]);
	      		detalle.put("valorUnitario", registro[5]);
	      		detalle.put("descuentoItem", registro[6]);
	      		detalle.put("montoIgvItem", registro[7]);
	      		detalle.put("afectaIgvItem", registro[8]);
	      		detalle.put("montoIscItem", registro[9]);
	      		detalle.put("tipoSistemaIsc", registro[10]);
	      		detalle.put("precioVentaUnitarioItem", registro[11]);
	      		detalle.put("valorVentaItem", registro[12]);
	     		
	      		// Valores Automaticos
	      		detalle.put("lineaSwf", linea);
	      		detalle.put("tipoCodiMoneGratiSwf",CONSTANTE_TIPO_CODIGO_MONEDA_GRATUITO);
	      			     		
	      		listaDetalle.add(detalle);
	      	}
	      }
	      bArchivoDetalle.close();
	      notaDebito.put("listaDetalle", listaDetalle);
	      
	      
	      /* Agregar informacion adicional */
          formatoComunes(notaDebito, archivoRelacionado, archivoAdiCabecera, archivoAdiDetalle, archivoLeyendas);
      
      return notaDebito;
      
	}
	
	
	@Override
	public void convertirAXml(String rutaEntrada, String nombreArchivo) throws Exception{
		File file = new File(rutaEntrada,nombreArchivo+".json");
		InputStream input = new FileInputStream(file);
		File fileSalida = new File(comunesService.obtenerRutaTrabajo(CONSTANTE_TEMP),nombreArchivo+".xml");
		OutputStream output = new FileOutputStream(fileSalida);
		JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).build();
		try{
			XMLEventReader reader = new JsonXMLInputFactory(config).createXMLEventReader(input);
			XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output,"ISO8859_1");
			writer = new PrettyXMLEventWriter(writer);
			writer.add(reader);
			reader.close();
			writer.close();
		}catch(Exception e){    
			log.error("Error: " + e.getMessage());
			throw new Exception("Error al convertir json a xml: " + e.getMessage());
		} finally {
		    output.close();
		    input.close();
		}			
				
	}
	
	private String transform(String dataXML, String inputXSL, String outputHTML) throws Exception
	{
			String retorno = "", mensaje="";
			
			File archivoXSL = new File(inputXSL);
			
			if(!archivoXSL.exists())
				throw new Exception("No existe la plantilla para el tipo documento a validar XML (Archivo XSL).");
			
			StreamSource xlsStreamSource = new StreamSource(new InputStreamReader(new FileInputStream(inputXSL), "ISO8859_1"));
			StreamSource xmlStreamSource = new StreamSource(new InputStreamReader(new FileInputStream(dataXML), "ISO8859_1"));
	
			TransformerFactory transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
	
			FileOutputStream fos = null;
		    try{
		    	fos = new FileOutputStream(outputHTML);
		    	Writer out = new OutputStreamWriter(fos, "ISO8859_1");
		    	Transformer transformer = transformerFactory.newTransformer(xlsStreamSource);
		    	transformer.transform(xmlStreamSource, new StreamResult(out));
		    	fos.close();
		    }catch(Exception e){
		    	try {
			    		String mensajeError = "", lineaArchivo="", nroObtenido="";
			    		Integer numeroLinea = 0;
			    		mensaje = e.getMessage();
		    			log.error("Error en Transform: " + mensaje);
		    			nroObtenido = FacturadorUtil.obtenerNumeroEnCadena(mensaje);
		    			if (nroObtenido.length()>0)numeroLinea = new Integer(nroObtenido);
		    			log.error("Error en Transform Numero de Linea: " + numeroLinea );
		    			lineaArchivo = FacturadorUtil.obtenerCodigoError(inputXSL,numeroLinea);
		    			log.error("Error en Transform Obtener Linea del Error: " + lineaArchivo );
		    			if (lineaArchivo.length()>0)nroObtenido = FacturadorUtil.obtenerNumeroEnCadena(lineaArchivo);
		    			log.error("Error en Transform Obtener Codigo del Error: " + nroObtenido );
						
		    			if("".equals(nroObtenido)){
							retorno = mensaje;
						}else{
							TxxxzBean txxxzBean = txxxzDAO.consultarErrorById(new Integer(nroObtenido));
							if(txxxzBean != null)
								mensajeError = txxxzBean.getNom_error();
							else
								mensajeError = mensaje;
							
							log.error("Error en Transform Obtener Descripcion del Error: " + mensajeError );
							retorno = nroObtenido + " - " +  mensajeError;
						}
		    			fos.close();
					}catch (Exception ex) {
						log.error("Error Ejecucion de Cierre Archivo: " + ex.getMessage());
						retorno = ex.getMessage();
					}
		    }
		    
		    return retorno;
		    
	}
	
	
	@Override
	public String firmarComprimirXml(String nombreArchivo) throws Exception {
		
		String rutaNombreEntrada = comunesService.obtenerRutaTrabajo(CONSTANTE_PARSE) + nombreArchivo + ".xml";
		String rutaNombreSalida = comunesService.obtenerRutaTrabajo(CONSTANTE_FIRMA) + nombreArchivo + ".xml";
				
	    FileInputStream inDocument = new FileInputStream(rutaNombreEntrada);
	    FileOutputStream fout = new FileOutputStream(rutaNombreSalida);

	    Map<String,Object> firma = this.firmarDocumento(inDocument);
        ByteArrayOutputStream outDocument = (ByteArrayOutputStream)firma.get("signatureFile");
        String digestValue = (String)firma.get("digestValue");
	     
	    outDocument.writeTo(fout);
	    fout.close();
	    	        
	    String rutaZipSalida = comunesService.obtenerRutaTrabajo(CONSTANTE_ENVIO) + nombreArchivo + ".zip";
	    OutputStream archivoZip = new FileOutputStream(rutaZipSalida);
	    InputStream archivoXml = new FileInputStream(rutaNombreSalida);
	    FacturadorUtil.comprimirArchivo(archivoZip, archivoXml, nombreArchivo + ".xml");
	    archivoZip.close();
	    archivoXml.close();
	    
	    return digestValue;
	    	
	}
	
	@Override	
	public String Desencriptar(String textoEncriptado) throws Exception {
      String secretKey = "qualityinfosolutions"; //llave para desenciptar datos
      String base64EncryptedString = "";

      byte[] message = Base64.decodeBase64(textoEncriptado.getBytes("utf-8"));
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
      byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
      SecretKey key = new SecretKeySpec(keyBytes, "DESede");

      Cipher decipher = Cipher.getInstance("DESede");
      decipher.init(Cipher.DECRYPT_MODE, key);

      byte[] plainText = decipher.doFinal(message);

      base64EncryptedString = new String(plainText, "UTF-8");

      return base64EncryptedString;
	}
	
	@Override
	public String Encriptar(String texto) throws Exception{
		 
      String secretKey = "qualityinfosolutions"; //llave para encriptar datos
      String base64EncryptedString = "";

      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
      byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);

      SecretKey key = new SecretKeySpec(keyBytes, "DESede");
      Cipher cipher = Cipher.getInstance("DESede");
      cipher.init(Cipher.ENCRYPT_MODE, key);

      byte[] plainTextBytes = texto.getBytes("utf-8");
      byte[] buf = cipher.doFinal(plainTextBytes);
      byte[] base64Bytes = Base64.encodeBase64(buf);
      base64EncryptedString = new String(base64Bytes);


      return base64EncryptedString;
	}
	
	
	@Override
	public Map<String,String> obtenerEstadoTicket(String rutaArchivo, String wsUrl, String nroTicket) throws Exception {
		
		TxxxyBean paramtroBean = new TxxxyBean();
		String param = "", numeroRUC = "", usuarioSOL = "", password = "";
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = null;
		listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
		if(listaParametros.size() > 0){
			for(TxxxyBean parametro : listaParametros ){
				param = parametro.getVal_para();
				if("NUMRUC".equals(parametro.getCod_para())) numeroRUC = param;
				if("USUSOL".equals(parametro.getCod_para())) usuarioSOL = param;
				if("CLASOL".equals(parametro.getCod_para())) password = this.Desencriptar(param);
			}
		}
		
		UsuarioSol usuarioSol = new UsuarioSol(numeroRUC, usuarioSOL, password);
		SunatGEMServiceWrapper client = new SunatGEMServiceWrapper(usuarioSol, wsUrl);
		Response respon = null; 
		
		respon = client.getStatus(rutaArchivo,nroTicket);
	
		Map<String,String> resultado = new HashMap<String,String>();		
		if(!respon.isError()) {
			Integer codError = respon.getCodigo();
			String msgError = respon.getMensaje();
			Integer pos = msgError.indexOf("Detalle:");
			if (pos > 0)msgError = msgError.substring(0, pos-1);
			if(codError.intValue()==0){
				resultado.put("situacion",CONSTANTE_SITUACION_ENVIADO_ACEPTADO);
				resultado.put("mensaje","-");
			}else{
				resultado.put("situacion",CONSTANTE_SITUACION_ENVIADO_ANULADO);
				resultado.put("mensaje",msgError);
			}
		}else{
			String msgError = respon.getMensaje();
			Integer pos = msgError.indexOf("Detalle:");
			if (pos > 0)msgError = msgError.substring(0, pos-1);
			resultado.put("situacion",CONSTANTE_SITUACION_ENVIADO_ANULADO);
			resultado.put("mensaje",msgError);
		}
		
		return resultado;
		
	}
	
	@Override
	public Map<String,String> enviarArchivoSunat(String wsUrl,String filename,String tipoComprobante) throws Exception {
		TxxxyBean paramtroBean = new TxxxyBean();
		Map<String,String> resultado = new HashMap<String,String>();
		String param = "", numeroRUC = "", usuarioSOL = "", password = "", mensaje="";
		
		synchronized(this){
			
			paramtroBean = new TxxxyBean();
			paramtroBean.setId_para("PARASIST");
			List<TxxxyBean> listaParametros = null;
			listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
			if(listaParametros.size() > 0){
				for(TxxxyBean parametro : listaParametros ){
					param = parametro.getVal_para();
					if("NUMRUC".equals(parametro.getCod_para())) numeroRUC = param;
					if("USUSOL".equals(parametro.getCod_para())) usuarioSOL = param;
					if("CLASOL".equals(parametro.getCod_para())) password = this.Desencriptar(param);
				}
			}
			
			String zipFile = filename + ".zip";
			UsuarioSol usuarioSol = new UsuarioSol(numeroRUC, usuarioSOL, password);
			SunatGEMServiceWrapper client = new SunatGEMServiceWrapper(usuarioSol, wsUrl);
			Response respon = null; 
			
			if(CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(tipoComprobante))
				respon = client.sendSummary(zipFile, comunesService.obtenerRutaTrabajo(CONSTANTE_ENVIO)+zipFile);
			else
				respon = client.sendBill(comunesService.obtenerRutaTrabajo(CONSTANTE_RPTA),zipFile, comunesService.obtenerRutaTrabajo(CONSTANTE_ENVIO)+zipFile);

			if(!respon.isError()) {
				Integer errorCode = respon.getCodigo();
				String msgError = respon.getMensaje();
				List<String> listaWarnings = respon.getWarnings()!=null?respon.getWarnings():new ArrayList<String>();
										
				if(!CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(tipoComprobante)){
					
					if(errorCode.intValue()==0&&listaWarnings.size()==0)
						resultado.put("situacion",CONSTANTE_SITUACION_ENVIADO_ACEPTADO);
					
					if(errorCode.intValue()==0&&listaWarnings.size()>0)
						resultado.put("situacion",CONSTANTE_SITUACION_ENVIADO_ACEPTADO_OBSERVACIONES);
					
					if(errorCode.intValue() > 0){
						/* Encontrar el codigo de Error*/
						TxxxzBean txxxzBean = txxxzDAO.consultarErrorById(errorCode);
						if(txxxzBean != null)
							mensaje = txxxzBean.getCod_error() + " - " + txxxzBean.getNom_error();
						else
							mensaje = msgError;
					
						resultado.put("situacion",CONSTANTE_SITUACION_ENVIADO_RECHAZADO);
						resultado.put("mensaje", mensaje);
						
					}
					
					if(errorCode.intValue() < 0){
						mensaje = "Error al invocar el servicio de SUNAT.";
						resultado.put("situacion",CONSTANTE_SITUACION_CON_ERRORES);
						resultado.put("mensaje", mensaje);
					}
					
				}else{
					
					mensaje = "Nro. Ticket: " + msgError; 
					resultado.put("situacion",CONSTANTE_SITUACION_ENVIADO_POR_PROCESAR);
					resultado.put("mensaje", mensaje);
					
				}
			
			}else{
				Integer errorCode = respon.getCodigo();
				String msgError = respon.getMensaje();
				if(CONSTANTE_CODIGO_ENVIO_PREVIO.compareTo(errorCode)==0&&!CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(tipoComprobante)){
				
					WebServiceConsultaWrapper consultaCdr = new WebServiceConsultaWrapper(); 
					String usuario = numeroRUC+usuarioSOL;
					String nombreArchivo = comunesService.obtenerRutaTrabajo(CONSTANTE_RPTA) + "R" + filename + ".zip";
					String[] datoArchivo = filename.split("\\-"); 
					String rucCdp=datoArchivo[0];
					String tipoCdp=datoArchivo[1];
					String serieCdp=datoArchivo[2];
					String nroCdp=datoArchivo[3];
					Integer estado = consultaCdr.obtenerEstadoCdr(usuario, password, nombreArchivo, rucCdp, tipoCdp, serieCdp, new Integer(nroCdp), CONSTANTE_CODIGO_EXITO_CONSULTA_CDR);
					if(estado.intValue()==0){
						mensaje = "-";
						resultado.put("situacion",CONSTANTE_SITUACION_ENVIADO_ACEPTADO);
						resultado.put("mensaje", mensaje);
					}else{
						
						if(estado.intValue()==1){
							mensaje = "-";
							resultado.put("situacion",CONSTANTE_SITUACION_ENVIADO_ACEPTADO_OBSERVACIONES);
							resultado.put("mensaje", mensaje);
						}else{
							
							if(estado.intValue()==-1){
								mensaje = "Error invocando al webservice para obtener CDR.";
								resultado.put("situacion",CONSTANTE_SITUACION_CON_ERRORES);
								resultado.put("mensaje", mensaje);
							}else{
								mensaje = "El webservice para obtener CDR, no se encontró el archivo CDR en SUNAT.";
								resultado.put("situacion",CONSTANTE_SITUACION_CON_ERRORES);
								resultado.put("mensaje", mensaje);
								
							}
							
						}	
						
					}
										
				}else{

					/* Encontrar el codigo de Error*/
					TxxxzBean txxxzBean = txxxzDAO.consultarErrorById(errorCode);
					if(txxxzBean != null)
						mensaje = txxxzBean.getCod_error() + " - " + txxxzBean.getNom_error();
					else
						mensaje = msgError;
				
					resultado.put("situacion",CONSTANTE_SITUACION_ENVIADO_ANULADO);
					resultado.put("mensaje", mensaje);
				}
			}
			
		}
		
		
		return resultado;
	}
	
	
	@Override
	public void formatoJsonPlantilla(HashMap<String,Object> objectoJson, String nombreArchivo) throws Exception{
		String plantillaSeleccionada = ""; 
		Map<String,Object> root = null;
		
		String idArchivo[] = nombreArchivo.split("\\-");
			
		
		if(CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(idArchivo[1])){
			root = this.generarResumenBajasJson(objectoJson,nombreArchivo);
			plantillaSeleccionada = "ConvertirRBajasXML.ftl";
		}
		
		if(CONSTANTE_TIPO_DOCUMENTO_FACTURA.equals(idArchivo[1])||CONSTANTE_TIPO_DOCUMENTO_BOLETA.equals(idArchivo[1])){
			root = this.generarFacturaJson(objectoJson,nombreArchivo);
			plantillaSeleccionada = "ConvertirFacturaXML.ftl";
		}
		
		if(CONSTANTE_TIPO_DOCUMENTO_NCREDITO.equals(idArchivo[1])){
			root = this.generarNotaJson(objectoJson,nombreArchivo);
			plantillaSeleccionada = "ConvertirNCreditoXML.ftl";
		}
		
		if(CONSTANTE_TIPO_DOCUMENTO_NDEBITO.equals(idArchivo[1])){
			root = this.generarNotaJson(objectoJson,nombreArchivo);
			plantillaSeleccionada = "ConvertirNDebitoXML.ftl";
		}
		
		
		File archivoFTL = new File(comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO), plantillaSeleccionada);
		
		if(!archivoFTL.exists())
			throw new Exception("No existe la plantilla para el tipo documento a generar XML (Archivo FTL).");
		
		Configuration cfg = new Configuration();
	      cfg.setDirectoryForTemplateLoading(new File(comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO)));
	      cfg.setDefaultEncoding("ISO8859_1");
	      cfg.setLocale(Locale.US);
	      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	     
	      Template temp = cfg.getTemplate(plantillaSeleccionada);
	      StringBuilder rutaSalida = new StringBuilder(); 
	      rutaSalida.setLength(0);
	      rutaSalida.append(comunesService.obtenerRutaTrabajo(CONSTANTE_TEMP))
	                .append(nombreArchivo)
	                .append(".xml");
	      OutputStream outputStream = new FileOutputStream(rutaSalida.toString());
	      Writer out = new OutputStreamWriter(outputStream);
	      temp.process(root, out);   
	      outputStream.close();        
		
		
		
	}
	
	@Override
	public String obtenerArchivoXml(String nombreArchivo) throws Exception{
		String contenido = "", linea = "";
		String archivoXml = nombreArchivo + ".xml";
		String archivoZip = "R" + nombreArchivo + ".zip";
		
		
		File archivoRespuesta = new File(comunesService.obtenerRutaTrabajo(CONSTANTE_RPTA),archivoZip);
		
		if(archivoRespuesta.exists()){
			File archivoContenido = new File(comunesService.obtenerRutaTrabajo(CONSTANTE_ENVIO),archivoXml);
			if(archivoContenido.exists()){
				FileReader fileReader = new FileReader(archivoContenido);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				StringBuffer cadenaLinea = new StringBuffer();
				cadenaLinea.setLength(0);
				while ((linea = bufferedReader.readLine()) != null) {
					cadenaLinea.append(linea);
					cadenaLinea.append("\n");
				}
				fileReader.close();
				
				contenido = cadenaLinea.toString();
			}
		}
		
		return contenido;
	}
	
	@Override
	public void adicionarInformacionFacturador(String nombreArchivoXml) throws Exception{
		
		
		/* Cargando Parametros del Facturador */
		TxxxyBean paramtroBean = new TxxxyBean();
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		paramtroBean.setCod_para("NUMRUC");
		List<TxxxyBean> listaParametros = txxxyDAO.consultarParametro(paramtroBean);
			
		
		/* Formando Nombre del Archivo */
		String documentoOrigen = comunesService.obtenerRutaTrabajo(CONSTANTE_DATA) + nombreArchivoXml + ".xml";
		String documentoSalida = comunesService.obtenerRutaTrabajo(CONSTANTE_TEMP) + nombreArchivoXml + ".xml";
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
		domFactory.setIgnoringComments(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder(); 
		Document document = builder.parse(new File(documentoOrigen)); 
		
		NodeList nodes = document.getElementsByTagName("cac:SignatoryParty");
		
		Text ruc = document.createTextNode(listaParametros.get(0).getVal_para()); 
		Element elementRuc = document.createElement("cbc:ID"); 
		elementRuc.appendChild(ruc); 
		nodes.item(0).getParentNode().insertBefore(elementRuc, nodes.item(0));
		
		Text texto = document.createTextNode("Elaborado por Sistema de Emision Electronica Facturador SUNAT (SEE-SFS) " + CONSTANTE_VERSION_SFS ); 
		Element elementTexto = document.createElement("cbc:Note"); 
		elementTexto.appendChild(texto); 
		nodes.item(0).getParentNode().insertBefore(elementTexto, nodes.item(0));

		Random calcularRnd = new Random(); 
		Integer codigoFacturadorSwf = (int)(calcularRnd.nextDouble() * 1000000);
		Text identificador = document.createTextNode(codigoFacturadorSwf.toString()); 
		Element elementIdentificador = document.createElement("cbc:ValidatorID"); 
		elementIdentificador.appendChild(identificador); 
		nodes.item(0).getParentNode().insertBefore(elementIdentificador, nodes.item(0));
		
		// writing xml file
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    DOMSource source = new DOMSource(document);
	    File outputFile = new File(documentoSalida);
	    StreamResult result = new StreamResult(outputFile );
	    // creating output stream
	    transformer.transform(source, result);
			
	} 
		
	
	@SuppressWarnings("unchecked")
	private void generarComunesJson(HashMap<String,Object> objectoJson, Map<String,Object> comprobante) throws Exception{
		/* Detalle del Comprobante */
		List<Map<String,Object>> detalle = objectoJson.get("detalle")!=null?(List<Map<String,Object>>)objectoJson.get("detalle"):new ArrayList<Map<String,Object>>();
		/* Adicionales de la Cabecera */
		Map<String,Object> cabecera = objectoJson.get("cabecera")!=null?(Map<String,Object>)objectoJson.get("cabecera"):new HashMap<String,Object>();
		/* Variables de Relacion*/
		List<Map<String,Object>> relacion = objectoJson.get("relacion")!=null?(List<Map<String,Object>>)objectoJson.get("relacion"):new ArrayList<Map<String,Object>>();
		String indDocRelacionado = "", tipDocRelacionado = "", numDocRelacionado = "", tipDocEmisor = "", numDocEmisor = "", mtoDocRelacionado = "0.00";
		/* Variables de Cabecera Adicionales */
		Map<String,Object> adiCabecera = null;
		String codRegiPercepcion = "",baseImponiblePercepcion = "",	montoPercepcion = "",	montoTotalSumPercepcion = "", totalVentaOperGratuita = "";
		String totalAnticipos = "",	codigoPaisCliente = "", codigoUbigeoCliente = "", direccionCliente = "", codigoPaisEntrega = "";
		String codigoUbigeoEntrega = "", direccionCompletaEntrega = "", fechaVencimiento = "";
		/* Variables de Detalle Adicional*/
		Map<String,Object> adicionalDetalle = null;
		String montoReferencial = "", numeroPlaca = "";
		/* Variables de la Leyenda */
		List<Map<String,Object>> listaLeyendaJson = objectoJson.get("leyendas")!=null?(List<Map<String,Object>>)objectoJson.get("leyendas"):new ArrayList<Map<String,Object>>();
		String codLeyenda = "", desLeyenda="";
		
		/* Adicional de Cabecera */
		adiCabecera = cabecera.get("adicionalCabecera")!=null?(Map<String,Object>)cabecera.get("adicionalCabecera"):null;
		if (adiCabecera != null){
			
			codRegiPercepcion = adiCabecera.get("codRegPercepcion")!=null?(String)adiCabecera.get("codRegPercepcion"):"";
			baseImponiblePercepcion = adiCabecera.get("mtoBaseImponiblePercepcion")!=null?(String)adiCabecera.get("mtoBaseImponiblePercepcion"):"";
			montoPercepcion = adiCabecera.get("mtoPercepcion")!=null?(String)adiCabecera.get("mtoPercepcion"):"";
			montoTotalSumPercepcion = adiCabecera.get("mtoTotalIncPercepcion")!=null?(String)adiCabecera.get("mtoTotalIncPercepcion"):"";
			totalVentaOperGratuita = adiCabecera.get("mtoOperGratuitas")!=null?(String)adiCabecera.get("mtoOperGratuitas"):"";
			totalAnticipos = adiCabecera.get("mtoTotalAnticipo")!=null?(String)adiCabecera.get("mtoTotalAnticipo"):"";
			codigoPaisCliente = adiCabecera.get("codPaisCliente")!=null?(String)adiCabecera.get("codPaisCliente"):"";
			codigoUbigeoCliente = adiCabecera.get("codUbigeoCliente")!=null?(String)adiCabecera.get("codUbigeoCliente"):"";
			direccionCliente = adiCabecera.get("desDireccionCliente")!=null?(String)adiCabecera.get("desDireccionCliente"):"";
			codigoPaisEntrega = adiCabecera.get("codPaisEntrega")!=null?(String)adiCabecera.get("codPaisEntrega"):"";
			codigoUbigeoEntrega = adiCabecera.get("codUbigeoEntrega")!=null?(String)adiCabecera.get("codUbigeoEntrega"):"";
			direccionCompletaEntrega = adiCabecera.get("desDireccionEntrega")!=null?(String)adiCabecera.get("desDireccionEntrega"):"";
			fechaVencimiento = adiCabecera.get("fecVencimiento")!=null?(String)adiCabecera.get("fecVencimiento"):"";
			
			// Desde Archivo 
			comprobante.put("codRegiPercepcion",codRegiPercepcion);
			comprobante.put("baseImponiblePercepcion",baseImponiblePercepcion);
			comprobante.put("montoPercepcion",montoPercepcion);
			comprobante.put("montoTotalSumPercepcion",montoTotalSumPercepcion);
			comprobante.put("totalVentaOperGratuita",totalVentaOperGratuita);
			comprobante.put("totalAnticipos",totalAnticipos);
			comprobante.put("codigoPaisCliente",codigoPaisCliente);
			comprobante.put("codigoUbigeoCliente",codigoUbigeoCliente);
			comprobante.put("direccionCliente",direccionCliente);
			comprobante.put("codigoPaisEntrega",codigoPaisEntrega);
			comprobante.put("codigoUbigeoEntrega",codigoUbigeoEntrega);
			comprobante.put("direccionCompletaEntrega",direccionCompletaEntrega);
			comprobante.put("fechaVencimiento",fechaVencimiento);
      		
			comprobante.put("codigoPercepSwf",CONSTANTE_ID_PER);
			comprobante.put("codigoGratuitoSwf",CONSTANTE_CODIGO_OPER_GRATUITA);
			comprobante.put("codigoMonedaSolesSwf",CONSTANTE_COD_MONEDA_SOLES);
			
			
			
		}
		 		
		Iterator<Map<String,Object>> listaDetalle = detalle.iterator();
		List<Map<String,Object>> detalleComprobante = comprobante.get("listaDetalle")!=null?(ArrayList<Map<String,Object>>)comprobante.get("listaDetalle"):new ArrayList<Map<String,Object>>();
  		/* Adicionales del Detalle */
  		for(Map<String,Object> entry: detalleComprobante){
  			adicionalDetalle = (Map<String,Object>)listaDetalle.next().get("adicionalDetalle");
  	  		if(adicionalDetalle != null){
  	  			montoReferencial = adicionalDetalle.get("mtoValorUnitarioGratuito")!=null?(String)adicionalDetalle.get("mtoValorUnitarioGratuito"):"";
  				numeroPlaca = adicionalDetalle.get("nroPlaca")!=null?(String)adicionalDetalle.get("nroPlaca"):"";
  				
  				entry.put("monto",montoReferencial);
  				entry.put("placa",numeroPlaca);
  				entry.put("tipoCodigoMonedaSwf",CONSTANTE_TIPO_CODIGO_MONEDA_ONEROSO);
  				entry.put("tipoCodigoPlacaSwf",CONSTANTE_TIPO_CODIGO_PLACA);
  	  		}
  		}
		
		/* Adicional de Lista de Relacionados */
		Iterator<Map<String,Object>> listaRelacionado = relacion.iterator();
		List<Map<String,Object>> listaRelaFactura = new ArrayList<Map<String,Object>>(); 
		Map<String,Object> relacionadoFactura = null;
		Map<String,Object> relacionadoLista = null;
		while(listaRelacionado.hasNext()){
			relacionadoLista = listaRelacionado.next();
			indDocRelacionado = relacionadoLista.get("indDocRelacionado")!=null?(String)relacionadoLista.get("indDocRelacionado"):"";
			tipDocRelacionado = relacionadoLista.get("tipDocRelacionado")!=null?(String)relacionadoLista.get("tipDocRelacionado"):"";
			numDocRelacionado = relacionadoLista.get("numDocRelacionado")!=null?(String)relacionadoLista.get("numDocRelacionado"):"";
			tipDocEmisor = relacionadoLista.get("tipDocEmisor")!=null?(String)relacionadoLista.get("tipDocEmisor"):"";
			numDocEmisor = relacionadoLista.get("numDocEmisor")!=null?(String)relacionadoLista.get("numDocEmisor"):"";
			mtoDocRelacionado = relacionadoLista.get("mtoDocRelacionado")!=null?(String)relacionadoLista.get("mtoDocRelacionado"):"0.00";
			
			// Desde Archivo Txt
      		relacionadoFactura = new HashMap<String,Object>();
      		relacionadoFactura.put("indDocuRelacionado", indDocRelacionado);
      		relacionadoFactura.put("tipDocuRelacionado", tipDocRelacionado);
      		relacionadoFactura.put("nroDocuRelacionado", numDocRelacionado);
      		relacionadoFactura.put("tipDocuEmisor", tipDocEmisor);
      		relacionadoFactura.put("nroDocuEmisor", numDocEmisor);
      		relacionadoFactura.put("mtoDocuRelacionado", mtoDocRelacionado);
     		
      		listaRelaFactura.add(relacionadoFactura);
			
		}
		comprobante.put("listaRelacionado", listaRelaFactura);
		
		/* Adicional de Leyendas de Lista de Relacionados */
		Iterator<Map<String,Object>> listaLeyendasBloque = listaLeyendaJson.iterator();
		List<Map<String,Object>> listaLeyendas = new ArrayList<Map<String,Object>>(); 
		Map<String,Object> leyendaJson = null;
		Map<String,Object> leyendaMap = null;
		while(listaLeyendasBloque.hasNext()){
			leyendaJson = listaLeyendasBloque.next();
			codLeyenda = leyendaJson.get("codLeyenda")!=null?(String)leyendaJson.get("codLeyenda"):"";
			desLeyenda = leyendaJson.get("desLeyenda")!=null?(String)leyendaJson.get("desLeyenda"):"";
			
			leyendaMap = new HashMap<String,Object>();
			leyendaMap.put("codigo",codLeyenda);
			leyendaMap.put("descripcion",desLeyenda);
			
      		listaLeyendas.add(leyendaMap);
			
		}
		comprobante.put("listaLeyendas", listaLeyendas);
		
		
		
	}
	
	@SuppressWarnings({ "unchecked"})
	private Map<String,Object> generarFacturaJson(HashMap<String,Object> objectoJson, String nombreArchivo) throws Exception{
		String nombreComercial="",ubigeo="", direccion="", param="",numRuc="",razonSocial="";
		
		/* Variables de Cabecera */
		Map<String,Object> cabecera = objectoJson.get("cabecera")!=null?(Map<String,Object>)objectoJson.get("cabecera"):new HashMap<String,Object>();
		String tipOperacion = cabecera.get("tipOperacion")!=null?(String)cabecera.get("tipOperacion"):"";
		String fecEmision = cabecera.get("fecEmision")!=null?(String)cabecera.get("fecEmision"):"";
		String codLocalEmisor = cabecera.get("codLocalEmisor")!=null?(String)cabecera.get("codLocalEmisor"):"";
		String numDocUsuario = cabecera.get("numDocUsuario")!=null?(String)cabecera.get("numDocUsuario"):"";
		String tipDocUsuario = cabecera.get("tipDocUsuario")!=null?(String)cabecera.get("tipDocUsuario"):"";
		String rznSocialUsuario = cabecera.get("rznSocialUsuario")!=null?(String)cabecera.get("rznSocialUsuario"):"";
		String tipMoneda = cabecera.get("tipMoneda")!=null?(String)cabecera.get("tipMoneda"):"";
		String sumDsctoGlobal = cabecera.get("sumDsctoGlobal")!=null?(String)cabecera.get("sumDsctoGlobal"):"0.00";
		String sumOtrosCargos = cabecera.get("sumOtrosCargos")!=null?(String)cabecera.get("sumOtrosCargos"):"0.00";
		String mtoDescuentos = cabecera.get("mtoDescuentos")!=null?(String)cabecera.get("mtoDescuentos"):"0.00";
		String mtoOperGravadas = cabecera.get("mtoOperGravadas")!=null?(String)cabecera.get("mtoOperGravadas"):"0.00";
		String mtoOperInafectas = cabecera.get("mtoOperInafectas")!=null?(String)cabecera.get("mtoOperInafectas"):"0.00";
		String mtoOperExoneradas = cabecera.get("mtoOperExoneradas")!=null?(String)cabecera.get("mtoOperExoneradas"):"0.00";
		String mtoIGV = cabecera.get("mtoIGV")!=null?(String)cabecera.get("mtoIGV"):"0.00";
		String mtoISC = cabecera.get("mtoISC")!=null?(String)cabecera.get("mtoISC"):"0.00";
		String mtoOtrosTributos = cabecera.get("mtoOtrosTributos")!=null?(String)cabecera.get("mtoOtrosTributos"):"0.00";
		String mtoImpVenta = cabecera.get("mtoImpVenta")!=null?(String)cabecera.get("mtoImpVenta"):"0.00";
		/* Variables de Detalle */
		String mtoValorUnitario = "0.00", mtoIscItem = "0.00", mtoPrecioVentaItem = "0.00", mtoValorVentaItem = "0.00";
		String mtoDsctoItem = "0.00", mtoIgvItem = "0.00", tipSisISC = "", tipAfeIGV = "0.00";
		String ctdUnidadItem = "0";
		String codUnidadMedida = "", codProducto = "",codProductoSUNAT = "",desItem = "";
		List<Map<String,Object>> detalle = objectoJson.get("detalle")!=null?(List<Map<String,Object>>)objectoJson.get("detalle"):new ArrayList<Map<String,Object>>(); 
		
		String idArchivo[] = nombreArchivo.split("\\-");
		/* Cargando PArametros de Firma */
		String identificadorFirmaSwf = "SIGN";
		Random calcularRnd = new Random(); 
		Integer codigoFacturadorSwf = (int)(calcularRnd.nextDouble() * 1000000); 
				
		/* Cargando Parametros del Facturador */
		TxxxyBean paramtroBean = new TxxxyBean();
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
		if(listaParametros.size() > 0){
			for(TxxxyBean parametro : listaParametros ){
				param = parametro.getVal_para();
				if("NUMRUC".equals(parametro.getCod_para())) numRuc = param;
				if("RAZON".equals(parametro.getCod_para())) razonSocial = param;
				if("NOMCOM".equals(parametro.getCod_para())) nombreComercial = param;
				if("UBIGEO".equals(parametro.getCod_para())) ubigeo = param;
				if("DIRECC".equals(parametro.getCod_para())) direccion = param;
			}
		}
		
		Map<String,Object> factura = null;
		// Desde Archivo Txt
		factura = new HashMap<String,Object>();
		factura.put("tipoOperacion", tipOperacion); // NUEVO
		factura.put("fechaEmision", fecEmision);
		factura.put("direccionUsuario", codLocalEmisor); // NUEVO
		factura.put("tipoDocumento", tipDocUsuario);
		factura.put("nroDocumento", numDocUsuario);
		factura.put("razonSocialUsuario", rznSocialUsuario);
		factura.put("moneda", tipMoneda);
		factura.put("descuentoGlobal",sumDsctoGlobal); // CAMBIO
		factura.put("sumaOtrosCargos", sumOtrosCargos);
		factura.put("totalDescuento", mtoDescuentos);  // CAMBIO
		factura.put("montoOperGravadas", mtoOperGravadas);
		factura.put("montoOperInafectas", mtoOperInafectas);
		factura.put("montoOperExoneradas", mtoOperExoneradas);
		factura.put("sumaIgv", mtoIGV);
		factura.put("sumaIsc", mtoISC);
		factura.put("sumaOtros", mtoOtrosTributos);
		factura.put("sumaImporteVenta", mtoImpVenta);
				
		// Valores Automaticos
		factura.put("ublVersionIdSwf", CONSTANTE_UBL_VERSION);
		factura.put("CustomizationIdSwf", CONSTANTE_CUSTOMIZATION_ID);
		factura.put("nroCdpSwf", idArchivo[2]+"-"+idArchivo[3]); // NUEVO
		factura.put("tipCdpSwf", idArchivo[1]); // NUEVO
		factura.put("nroRucEmisorSwf", numRuc);
		factura.put("tipDocuEmisorSwf", CONSTANTE_TIPO_DOCU_EMISOR);
		factura.put("nombreComercialSwf", nombreComercial);
		factura.put("razonSocialSwf", razonSocial);
		factura.put("ubigeoDomFiscalSwf", ubigeo);
		factura.put("direccionDomFiscalSwf", direccion);
		factura.put("paisDomFiscalSwf", CONSTANTE_CODIGO_PAIS);
		factura.put("codigoMontoDescuentosSwf", CONSTANTE_CODIGO_MONTO_DSCTO);
		factura.put("codigoMontoOperGravadasSwf", CONSTANTE_CODIGO_OPER_GRAVADA);
		factura.put("codigoMontoOperInafectasSwf", CONSTANTE_CODIGO_OPER_INAFECTA);
		factura.put("codigoMontoOperExoneradasSwf", CONSTANTE_CODIGO_OPER_EXONERADA);
		factura.put("idIgv", CONSTANTE_ID_IVG);
		factura.put("codIgv", CONSTANTE_COD_IVG);
		factura.put("codExtIgv", CONSTANTE_COD_EXT_IVG);
		factura.put("idIsc", CONSTANTE_ID_ISC);
		factura.put("codIsc", CONSTANTE_COD_ISC);
		factura.put("codExtIsc", CONSTANTE_COD_EXT_ISC);
		factura.put("idOtr", CONSTANTE_ID_OTR);
		factura.put("codOtr", CONSTANTE_COD_OTR);
		factura.put("codExtOtr", CONSTANTE_COD_EXT_OTR);
		factura.put("tipoCodigoMonedaSwf",CONSTANTE_TIPO_CODIGO_MONEDA_ONEROSO);
		
		factura.put("identificadorFacturadorSwf",CONSTANTE_INFO_SFS_SUNAT + CONSTANTE_VERSION_SFS);
		factura.put("codigoFacturadorSwf",codigoFacturadorSwf.toString());
		factura.put("identificadorFirmaSwf",identificadorFirmaSwf);
		
		
		Iterator<Map<String,Object>> listaDetalle = detalle.iterator();
		List<Map<String,Object>> listaDetaFactura = new ArrayList<Map<String,Object>>(); 
		Map<String,Object> detalleFactura = null;
		Map<String,Object> detalleLista = null;
		Integer linea = new Integer(0);
		while(listaDetalle.hasNext()){
			detalleLista = listaDetalle.next();
			codUnidadMedida = detalleLista.get("codUnidadMedida")!=null?(String)detalleLista.get("codUnidadMedida"):"";
			ctdUnidadItem = detalleLista.get("ctdUnidadItem")!=null?(String)detalleLista.get("ctdUnidadItem"):"0";
			codProducto = detalleLista.get("codProducto")!=null?(String)detalleLista.get("codProducto"):"";
			codProductoSUNAT = detalleLista.get("codProductoSUNAT")!=null?(String)detalleLista.get("codProductoSUNAT"):"";
			desItem = detalleLista.get("desItem")!=null?(String)detalleLista.get("desItem"):"";
			mtoValorUnitario = detalleLista.get("mtoValorUnitario")!=null?(String)detalleLista.get("mtoValorUnitario"):"0.00";
			mtoDsctoItem = detalleLista.get("mtoDsctoItem")!=null?(String)detalleLista.get("mtoDsctoItem"):"0.00";
			mtoIgvItem = detalleLista.get("mtoIgvItem")!=null?(String)detalleLista.get("mtoIgvItem"):"0.00";
			tipAfeIGV = detalleLista.get("tipAfeIGV")!=null?(String)detalleLista.get("tipAfeIGV"):"0.00";
			mtoIscItem = detalleLista.get("mtoIscItem")!=null?(String)detalleLista.get("mtoIscItem"):"0.00";
			tipSisISC = detalleLista.get("tipSisISC")!=null?(String)detalleLista.get("tipSisISC"):"";
			mtoPrecioVentaItem = detalleLista.get("mtoPrecioVentaItem")!=null?(String)detalleLista.get("mtoPrecioVentaItem"):"0.00";
			mtoValorVentaItem = detalleLista.get("mtoValorVentaItem")!=null?(String)detalleLista.get("mtoValorVentaItem"):"0.00";
			
			// Desde Archivo Txt
      		linea = linea + 1;
      		detalleFactura = new HashMap<String,Object>();
      		detalleFactura.put("unidadMedida", codUnidadMedida);
      		detalleFactura.put("cantItem", ctdUnidadItem);
      		detalleFactura.put("codiProducto", codProducto);
      		detalleFactura.put("codiSunat", codProductoSUNAT);
      		detalleFactura.put("desItem", desItem);
      		detalleFactura.put("valorUnitario", mtoValorUnitario);
      		detalleFactura.put("descuentoItem", mtoDsctoItem);
      		detalleFactura.put("montoIgvItem", mtoIgvItem);
      		detalleFactura.put("afectaIgvItem", tipAfeIGV);
      		detalleFactura.put("montoIscItem", mtoIscItem);
      		detalleFactura.put("tipoSistemaIsc", tipSisISC);
      		detalleFactura.put("precioVentaUnitarioItem", mtoPrecioVentaItem);
      		detalleFactura.put("valorVentaItem", mtoValorVentaItem);
      		     		
      		// Valores Automaticos
      		detalleFactura.put("lineaSwf", linea);
      		detalleFactura.put("tipoCodiMoneGratiSwf",CONSTANTE_TIPO_CODIGO_MONEDA_GRATUITO);
     		
      		listaDetaFactura.add(detalleFactura);
			
		}
		factura.put("listaDetalle", listaDetaFactura);
		
		generarComunesJson(objectoJson,factura);
			
		return factura;
		
	}
		
	
	@SuppressWarnings({ "unchecked"})
	private Map<String,Object> generarNotaJson(HashMap<String,Object> objectoJson, String nombreArchivo) throws Exception{
		
		String nombreComercial="",ubigeo="", direccion="", param="",numRuc="",razonSocial="";
		
		/* Variables de Cabecera */
		Map<String,Object> cabecera = objectoJson.get("cabecera")!=null?(Map<String,Object>)objectoJson.get("cabecera"):new HashMap<String,Object>();
		String fecEmision = cabecera.get("fecEmision")!=null?(String)cabecera.get("fecEmision"):"";
		String codMotivo = cabecera.get("codMotivo")!=null?(String)cabecera.get("codMotivo"):"";
		String desMotivo = cabecera.get("desMotivo")!=null?(String)cabecera.get("desMotivo"):"";
		String numDocModifica = cabecera.get("numDocAfectado")!=null?(String)cabecera.get("numDocAfectado"):"";
		String tipDocModifica = cabecera.get("tipDocAfectado")!=null?(String)cabecera.get("tipDocAfectado"):"";
		String numDocUsuario = cabecera.get("numDocUsuario")!=null?(String)cabecera.get("numDocUsuario"):"";
		String tipDocUsuario = cabecera.get("tipDocUsuario")!=null?(String)cabecera.get("tipDocUsuario"):"";
		String rznSocialUsuario = cabecera.get("rznSocialUsuario")!=null?(String)cabecera.get("rznSocialUsuario"):"";
		String tipMoneda = cabecera.get("tipMoneda")!=null?(String)cabecera.get("tipMoneda"):"";
		String sumOtrosCargos = cabecera.get("sumOtrosCargos")!=null?(String)cabecera.get("sumOtrosCargos"):"0.00";
		String mtoOperGravadas = cabecera.get("mtoOperGravadas")!=null?(String)cabecera.get("mtoOperGravadas"):"0.00";
		String mtoOperInafectas = cabecera.get("mtoOperInafectas")!=null?(String)cabecera.get("mtoOperInafectas"):"0.00";
		String mtoOperExoneradas = cabecera.get("mtoOperExoneradas")!=null?(String)cabecera.get("mtoOperExoneradas"):"0.00";
		String mtoIGV = cabecera.get("mtoIGV")!=null?(String)cabecera.get("mtoIGV"):"0.00";
		String mtoISC = cabecera.get("mtoISC")!=null?(String)cabecera.get("mtoISC"):"0.00";
		String mtoOtrosTributos = cabecera.get("mtoOtrosTributos")!=null?(String)cabecera.get("mtoOtrosTributos"):"0.00";
		String mtoImpVenta = cabecera.get("mtoImpVenta")!=null?(String)cabecera.get("mtoImpVenta"):"0.00";
		/* Variables de Detalle */
		String mtoValorUnitario = "0.00", mtoIscItem = "0.00", mtoPrecioVentaItem = "0.00", mtoValorVentaItem = "0.00";
		String mtoDsctoItem = "0.00", mtoIgvItem = "0.00", tipSisISC = "", tipAfeIGV = "0.00";
		String ctdUnidadItem = "0";
		String   codUnidadMedida = "", codProducto = "",codProductoSUNAT = "",desItem = "";
		List<Map<String,Object>> detalle = objectoJson.get("detalle")!=null?(List<Map<String,Object>>)objectoJson.get("detalle"):new ArrayList<Map<String,Object>>(); 
		
		
		String idArchivo[] = nombreArchivo.split("\\-");
		/* Cargando PArametros de Firma */
		String identificadorFirmaSwf = "SIGN";
		Random calcularRnd = new Random(); 
		Integer codigoFacturadorSwf = (int)(calcularRnd.nextDouble() * 1000000); 
		
		
		/* Cargando Parametros del Facturador */
		TxxxyBean paramtroBean = new TxxxyBean();
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
		if(listaParametros.size() > 0){
			for(TxxxyBean parametro : listaParametros ){
				param = parametro.getVal_para();
				if("NUMRUC".equals(parametro.getCod_para())) numRuc = param;
				if("RAZON".equals(parametro.getCod_para())) razonSocial = param;
				if("NOMCOM".equals(parametro.getCod_para())) nombreComercial = param;
				if("UBIGEO".equals(parametro.getCod_para())) ubigeo = param;
				if("DIRECC".equals(parametro.getCod_para())) direccion = param;
			}
		}
		
		Map<String,Object> nota = null;
		// Desde Archivo Txt
		nota = new HashMap<String,Object>();
		nota.put("fechaEmision", fecEmision);

		nota.put("codigoMotivo", codMotivo);
		nota.put("descripcionMotivo", desMotivo);
		nota.put("tipoDocuModifica", tipDocModifica);
		nota.put("nroDocuModifica", numDocModifica);
		nota.put("tipoDocuIdenti", tipDocUsuario);
		nota.put("nroDocuIdenti", numDocUsuario);
		nota.put("razonSocialUsuario", rznSocialUsuario);
		nota.put("moneda", tipMoneda);
		nota.put("sumaOtrosCargos", sumOtrosCargos);
		nota.put("montoOperGravadas", mtoOperGravadas);
		nota.put("montoOperInafectas", mtoOperInafectas);
		nota.put("montoOperExoneradas", mtoOperExoneradas);
		nota.put("sumaIgv", mtoIGV);
		nota.put("sumaIsc", mtoISC);
		nota.put("sumaOtros", mtoOtrosTributos);
		nota.put("sumaImporteVenta", mtoImpVenta);
		
		// Valores Automaticos
		nota.put("ublVersionIdSwf", CONSTANTE_UBL_VERSION);
		nota.put("CustomizationIdSwf", CONSTANTE_CUSTOMIZATION_ID);
		nota.put("nroCdpSwf", idArchivo[2]+"-"+idArchivo[3]); // NUEVO
		nota.put("tipCdpSwf", idArchivo[1]); // NUEVO
		nota.put("nroRucEmisorSwf", numRuc);
		nota.put("tipDocuEmisorSwf", CONSTANTE_TIPO_DOCU_EMISOR);
		nota.put("nombreComercialSwf", nombreComercial);
		nota.put("razonSocialSwf", razonSocial);
		nota.put("ubigeoDomFiscalSwf", ubigeo);
		nota.put("direccionDomFiscalSwf", direccion);
		nota.put("paisDomFiscalSwf", CONSTANTE_CODIGO_PAIS);
		nota.put("codigoMontoDescuentosSwf", CONSTANTE_CODIGO_MONTO_DSCTO);
		nota.put("codigoMontoOperGravadasSwf", CONSTANTE_CODIGO_OPER_GRAVADA);
		nota.put("codigoMontoOperInafectasSwf", CONSTANTE_CODIGO_OPER_INAFECTA);
		nota.put("codigoMontoOperExoneradasSwf", CONSTANTE_CODIGO_OPER_EXONERADA);
		nota.put("idIgv", CONSTANTE_ID_IVG);
		nota.put("codIgv", CONSTANTE_COD_IVG);
		nota.put("codExtIgv", CONSTANTE_COD_EXT_IVG);
		nota.put("idIsc", CONSTANTE_ID_ISC);
		nota.put("codIsc", CONSTANTE_COD_ISC);
		nota.put("codExtIsc", CONSTANTE_COD_EXT_ISC);
		nota.put("idOtr", CONSTANTE_ID_OTR);
		nota.put("codOtr", CONSTANTE_COD_OTR);
		nota.put("codExtOtr", CONSTANTE_COD_EXT_OTR);
		nota.put("tipoCodigoMonedaSwf",CONSTANTE_TIPO_CODIGO_MONEDA_ONEROSO);
		
		nota.put("identificadorFacturadorSwf",CONSTANTE_INFO_SFS_SUNAT + CONSTANTE_VERSION_SFS);
		nota.put("codigoFacturadorSwf",codigoFacturadorSwf.toString());
		nota.put("identificadorFirmaSwf",identificadorFirmaSwf);
		
		
		Iterator<Map<String,Object>> listaDetalle = detalle.iterator();
		List<Map<String,Object>> listaDetaNota = new ArrayList<Map<String,Object>>(); 
		Map<String,Object> detalleNota = null;
		Map<String,Object> detalleLista = null;
		Integer linea = new Integer(0);
		while(listaDetalle.hasNext()){
			detalleLista = listaDetalle.next();
			codUnidadMedida = detalleLista.get("codUnidadMedida")!=null?(String)detalleLista.get("codUnidadMedida"):"";
			ctdUnidadItem = detalleLista.get("ctdUnidadItem")!=null?(String)detalleLista.get("ctdUnidadItem"):"0";
			codProducto = detalleLista.get("codProducto")!=null?(String)detalleLista.get("codProducto"):"";
			codProductoSUNAT = detalleLista.get("codProductoSUNAT")!=null?(String)detalleLista.get("codProductoSUNAT"):"";
			desItem = detalleLista.get("desItem")!=null?(String)detalleLista.get("desItem"):"";
			mtoValorUnitario = detalleLista.get("mtoValorUnitario")!=null?(String)detalleLista.get("mtoValorUnitario"):"0.00";
			mtoDsctoItem = detalleLista.get("mtoDsctoItem")!=null?(String)detalleLista.get("mtoDsctoItem"):"0.00";
			mtoIgvItem = detalleLista.get("mtoIgvItem")!=null?(String)detalleLista.get("mtoIgvItem"):"0.00";
			tipAfeIGV = detalleLista.get("tipAfeIGV")!=null?(String)detalleLista.get("tipAfeIGV"):"0.00";
			mtoIscItem = detalleLista.get("mtoIscItem")!=null?(String)detalleLista.get("mtoIscItem"):"0.00";
			tipSisISC = detalleLista.get("tipSisISC")!=null?(String)detalleLista.get("tipSisISC"):"";
			mtoPrecioVentaItem = detalleLista.get("mtoPrecioVentaItem")!=null?(String)detalleLista.get("mtoPrecioVentaItem"):"0.00";
			mtoValorVentaItem = detalleLista.get("mtoValorVentaItem")!=null?(String)detalleLista.get("mtoValorVentaItem"):"0.00";
			
			// Desde Archivo Txt
      		linea = linea + 1;
      		detalleNota = new HashMap<String,Object>();
      		detalleNota.put("unidadMedida", codUnidadMedida);
      		detalleNota.put("cantItem", ctdUnidadItem);
      		detalleNota.put("codiProducto", codProducto);
      		detalleNota.put("codiSunat", codProductoSUNAT);
      		detalleNota.put("desItem", desItem);
      		detalleNota.put("valorUnitario", mtoValorUnitario);
      		detalleNota.put("descuentoItem", mtoDsctoItem);
      		detalleNota.put("montoIgvItem", mtoIgvItem);
      		detalleNota.put("afectaIgvItem", tipAfeIGV);
      		detalleNota.put("montoIscItem", mtoIscItem);
      		detalleNota.put("tipoSistemaIsc", tipSisISC);
      		detalleNota.put("precioVentaUnitarioItem", mtoPrecioVentaItem);
      		detalleNota.put("valorVentaItem", mtoValorVentaItem);
     		
      		// Valores Automaticos
      		detalleNota.put("lineaSwf", linea);
      		detalleNota.put("tipoCodiMoneGratiSwf",CONSTANTE_TIPO_CODIGO_MONEDA_GRATUITO);
     		
      		listaDetaNota.add(detalleNota);
			
		}
		nota.put("listaDetalle", listaDetaNota);
		
		generarComunesJson(objectoJson,nota);
				
		return nota;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private Map<String,Object> generarResumenBajasJson(HashMap<String,Object> objectoJson, String nombreArchivo) throws Exception{
		List<Map<String,Object>> listaResumen = objectoJson.get("resumenBajas")!=null?(List<Map<String,Object>>)objectoJson.get("resumenBajas"):new ArrayList<Map<String,Object>>();
		String numRuc = "", razonSocial = "", param ="",nombreComercial="";
		/* Cargando Parametros del Facturador */
		TxxxyBean paramtroBean = new TxxxyBean();
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
		if(listaParametros.size() > 0){
			for(TxxxyBean parametro : listaParametros ){
				param = parametro.getVal_para();
				if("NOMCOM".equals(parametro.getCod_para())) nombreComercial = param;
				if("NUMRUC".equals(parametro.getCod_para())) numRuc = param;
				if("RAZON".equals(parametro.getCod_para())) razonSocial = param;
			}
		}
		
		// Generando ID de Comunicacion
		String idArchivo[] = nombreArchivo.split("\\-");
		String idComunicacion = idArchivo[1]+"-"+idArchivo[2]+"-"+idArchivo[3];
		
		/* Cargando PArametros de Firma */
		String identificadorFirmaSwf = "SIGN";
		Random calcularRnd = new Random(); 
		Integer codigoFacturadorSwf = (int)(calcularRnd.nextDouble() * 1000000); 
		
		/* Leyendo Archivo de Cabecera de resumen de bajas */
		Iterator<Map<String,Object>> listaDetalle = listaResumen.iterator();
		List<Map<String,Object>> listaResumenBajas = new ArrayList<Map<String,Object>>();
		String fechaDocumentoBaja="", fechaComunicacionBaja = "", tipoDocumento = "", serieNumeroDocumento = "", motivoBajaDocumento = "";
		Map<String,Object> bajaResumenJson = null;
		Map<String,Object> resumenBajas = null;
		Map<String,Object> resumenGeneral = new HashMap<String,Object>();
		Integer linea = 0;
		while(listaDetalle.hasNext()){
			// Linea de Comunicacion
			linea = linea + 1 ;
			bajaResumenJson = listaDetalle.next();

			fechaDocumentoBaja = bajaResumenJson.get("fecGeneracion")!=null?(String)bajaResumenJson.get("fecGeneracion"):"";
			fechaComunicacionBaja = bajaResumenJson.get("fecComunicacion")!=null?(String)bajaResumenJson.get("fecComunicacion"):"";
			tipoDocumento = bajaResumenJson.get("tipDocBaja")!=null?(String)bajaResumenJson.get("tipDocBaja"):"";
			serieNumeroDocumento = bajaResumenJson.get("numDocBaja")!=null?(String)bajaResumenJson.get("numDocBaja"):"";
			motivoBajaDocumento = bajaResumenJson.get("desMotivoBaja")!=null?(String)bajaResumenJson.get("desMotivoBaja"):"";
									
			String nroDocumento[] = serieNumeroDocumento.split("\\-");
			
			
			if(linea == 1){
				resumenGeneral.put("nombreComercialSwf", nombreComercial);
				resumenGeneral.put("razonSocialSwf", razonSocial);
				resumenGeneral.put("nroRucEmisorSwf", numRuc);
				resumenGeneral.put("tipDocuEmisorSwf", CONSTANTE_TIPO_DOCU_EMISOR);
				resumenGeneral.put("fechaDocumentoBaja", fechaDocumentoBaja);
				resumenGeneral.put("fechaComunicacioBaja", fechaComunicacionBaja);
				// Valores Automaticos
				resumenGeneral.put("ublVersionIdSwf", CONSTANTE_UBL_VERSION);
				resumenGeneral.put("idComunicacion", idComunicacion);
				resumenGeneral.put("CustomizationIdSwf", CONSTANTE_CUSTOMIZATION_ID);
				
				resumenGeneral.put("identificadorFacturadorSwf",CONSTANTE_INFO_SFS_SUNAT + CONSTANTE_VERSION_SFS);
				resumenGeneral.put("codigoFacturadorSwf",codigoFacturadorSwf.toString());
				resumenGeneral.put("identificadorFirmaSwf",identificadorFirmaSwf);
				
				resumenBajas = new HashMap<String,Object>();
				resumenBajas.put("tipoDocumentoBaja", tipoDocumento);
				resumenBajas.put("serieDocumentoBaja", nroDocumento[0]);
				resumenBajas.put("nroDocumentoBaja", nroDocumento[1]);
				resumenBajas.put("motivoBajaDocumento", motivoBajaDocumento);
				resumenBajas.put("linea",linea);

				
			}else{
				
				resumenBajas = new HashMap<String,Object>();
				resumenBajas.put("tipoDocumentoBaja", tipoDocumento);
				resumenBajas.put("serieDocumentoBaja", nroDocumento[0]);
				resumenBajas.put("nroDocumentoBaja", nroDocumento[1]);
				resumenBajas.put("motivoBajaDocumento", motivoBajaDocumento);
				resumenBajas.put("linea",linea);
			}
		
			listaResumenBajas.add(resumenBajas);
		  
		}
		
		resumenGeneral.put("listaResumen", listaResumenBajas);
      
		return resumenGeneral;
	}
	
	
	private Map<String,Object> firmarDocumento(InputStream inDocument) throws Exception {
		Map<String,Object> retorno = new HashMap<String,Object>();
		// Buscar Parametro de Clave passPrivateKey
		TxxxyBean paramtroBean = new TxxxyBean();
		String param = "", clavePrivateKey = "";
		paramtroBean = new TxxxyBean();
		paramtroBean.setId_para("PARASIST");
		List<TxxxyBean> listaParametros = null;
		listaParametros = txxxyDAO.consultarParametroById(paramtroBean);
		if(listaParametros.size() > 0){
			for(TxxxyBean parametro : listaParametros ){
				param = parametro.getVal_para();
				if("PRKCRT".equals(parametro.getCod_para())) clavePrivateKey = this.Desencriptar(param);
			}
		}
		
		// Cargamos el almacen de claves
		KeyStore ks  = KeyStore.getInstance(KEYSTORE_TYPE);
		// Obtenemos la clave privada, pues la necesitaremos para firmar.		
		ks.load(new FileInputStream(comunesService.obtenerRutaTrabajo(CONSTANTE_ALMCERT)+"FacturadorKey.jks"), KEYSTORE_PASSWORD.toCharArray());
		
		// get my private key
		PrivateKey privateKey = (PrivateKey)ks.getKey(PRIVATE_KEY_ALIAS, clavePrivateKey.toCharArray());
		
		// Añadimos el KeyInfo del certificado cuya clave privada usamos
		X509Certificate cert = (X509Certificate) ks.getCertificate(PRIVATE_KEY_ALIAS);
		ByteArrayOutputStream signatureFile = new ByteArrayOutputStream();

	    Document doc = buildDocument(inDocument);
	    Node parentNode = addExtensionContent(doc);

	    String idReference = "SignSUNAT";

	    XMLSignatureFactory fac = XMLSignatureFactory.getInstance();

	    Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA1, null),Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
	    	    
	    SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
		    fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));
	    	    	    	    
	    KeyInfoFactory kif = fac.getKeyInfoFactory();
	    List<X509Certificate> x509Content = new ArrayList<X509Certificate>();
	    x509Content.add(cert);
	    X509Data xd = kif.newX509Data(x509Content);
	    KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

	    DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());
	    XMLSignature signature = fac.newXMLSignature(si, ki);
	        
	    if (parentNode != null)
		dsc.setParent(parentNode);
	    dsc.setDefaultNamespacePrefix("ds");
	    signature.sign(dsc);
	    	        	    
	    String digestValue = "-";
	    Element elementParent = (Element) dsc.getParent();
	    if (idReference != null && elementParent.getElementsByTagName("ds:Signature") != null) {
	    	Element elementSignature = (Element) elementParent.getElementsByTagName("ds:Signature").item(0);
	    	elementSignature.setAttribute("Id", idReference);
	    	
	    	NodeList nodeList = elementParent.getElementsByTagName("ds:DigestValue");
	 	    for (int i = 0; i < nodeList.getLength(); i++) {
	 	    	digestValue = this.obtenerNodo(nodeList.item(i));
	 	    }
	    }	    

	    FacturadorUtil.outputDocToOutputStream(doc, signatureFile);
	    signatureFile.close();
	    
	    retorno.put("signatureFile",signatureFile);
	    retorno.put("digestValue",digestValue);
	    
	    return retorno;
    }

    
	private String obtenerNodo(Node node) throws Exception {
	    StringBuffer valorClave = new StringBuffer();
	    valorClave.setLength(0);
	    
	    Integer tamano = node.getChildNodes().getLength();
	    
	    for (int i = 0; i < tamano; i ++) {
	      Node c = node.getChildNodes().item(i);
	      if (c.getNodeType() == Node.TEXT_NODE) {
	    	  valorClave.append(c.getNodeValue());
	      }
	    }
	    
	    String nodo = valorClave.toString().trim(); 
	    
	    return nodo;
	  }
	
    private Document buildDocument(InputStream inDocument) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
	    dbf.setAttribute("http://xml.org/sax/features/namespaces", Boolean.TRUE);
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Reader reader = new InputStreamReader(inDocument,"ISO8859_1");
	    Document doc = db.parse(new InputSource(reader));
	    return doc;
    }

    private Node addExtensionContent(Document doc) {
		NodeList nodeList = doc.getDocumentElement().getElementsByTagNameNS("urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2", "UBLExtensions");
		Node extensions = nodeList.item(0);
		extensions.appendChild(doc.createTextNode("\n\t\t"));
		Node extension = doc.createElement("ext:UBLExtension");
		extension.appendChild(doc.createTextNode("\n\t\t\t"));
		Node content = doc.createElement("ext:ExtensionContent");
		extension.appendChild(content);
		extension.appendChild(doc.createTextNode("\n\t\t"));
		extensions.appendChild(extension);
		extensions.appendChild(doc.createTextNode("\n\t"));
		return content;
    }  
    

}
