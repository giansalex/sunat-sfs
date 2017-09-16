package sfs.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRXmlUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import sfs.model.dao.TxxxzDAO;
import sfs.model.domain.TxxxzBean;
import sfs.util.FacturadorUtil;
import static sfs.util.Constantes.CONSTANTE_FORMATO;
import static sfs.util.Constantes.CONSTANTE_FIRMA;

@Service
public class ReporteDocumentosServiceImpl implements ReporteDocumentosService{
	
	private static final Log log = LogFactory.getLog(ReporteDocumentosServiceImpl.class);
	

	@Autowired
	private TxxxzDAO txxxzDAO;
	
	@Autowired
	private ComunesService comunesService;
	
	@Autowired
	private ReporteDocumentosAdicionalService reporteDocumentosAdicionalService;

	@Override
	public void imprimirComprobante(String nombreReporteJasper, String nombreArchivoSalida, String nombreArchivoXml, String rutaBaseXml, String nombreArchivo) throws Exception{
		
			
		try{
				/* Se depura el archivo xml de los namespaces */
				String archivoXsl = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO) + "Depura_Xml_Impresion.xsl";
				String archivoOrigen = comunesService.obtenerRutaTrabajo(CONSTANTE_FIRMA) + nombreArchivo + ".xml";
				this.transform(archivoOrigen, archivoXsl, nombreArchivoXml);
				
				/* Obtener los datos para generar QR */
				String rutaImagenQr = reporteDocumentosAdicionalService.generarCodigoQr(nombreArchivoXml);
								
				Document document = JRXmlUtils.parse(JRLoader.getLocationInputStream(nombreArchivoXml));
				JasperReport jasperReport = (JasperReport)JRLoader.loadObject(new File(nombreReporteJasper));
				
				JRXmlDataSource xmlDataSource = new JRXmlDataSource(nombreArchivoXml,rutaBaseXml);
				
				Map<String, Object> parametros = new HashMap<String, Object>();
		        parametros.put("RUTA_IMAGEN_QR", rutaImagenQr);
				parametros.put("XML_DATA_DOCUMENT", document);
		        parametros.put("SUBREPORT_DIR", comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO));
		        
				
			    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, xmlDataSource);	
				JasperExportManager.exportReportToPdfFile(jasperPrint, nombreArchivoSalida);

		}catch(Exception e){
	    	log.error("Error Generado en ReporteDocumentosServiceImpl.imprimirComprobante: " + e.getMessage() + " Causa: " + e.getCause());
	    	throw new Exception("Error Generado en ReporteDocumentosServiceImpl.imprimirComprobante:  " + e.getMessage() + " Causa: " + e.getCause());
	    }
		 
	}
	
	
	private String transform(String dataXML, String inputXSL, String outputHTML) throws Exception
	{
			String retorno = "", mensaje="";
			
			File archivoXSL = new File(inputXSL);
			
			if(!archivoXSL.exists())
				throw new Exception("No existe la plantilla para el tipo documento a validar XML (Archivo XSL).");
			
			StreamSource xlsStreamSource = new StreamSource(inputXSL);
			StreamSource xmlStreamSource = new StreamSource(dataXML);
	
			TransformerFactory transformerFactory = TransformerFactory.newInstance(
		            "net.sf.saxon.TransformerFactoryImpl", null);
	
			FileOutputStream fos = null;
		    try{
		    	fos = new FileOutputStream(outputHTML);
		    	
		    	Transformer transformer = transformerFactory.newTransformer(xlsStreamSource);
		    	transformer.transform(xmlStreamSource, new StreamResult(fos));
		    	fos.close();
		    }catch(Exception e){
		    	try {
			    		String mensajeError = "", lineaArchivo="", nroObtenido="";
			    		Integer numeroLinea = 0;
			    		mensaje = e.getMessage();
		    			nroObtenido = FacturadorUtil.obtenerNumeroEnCadena(mensaje);
		    			if (nroObtenido.length()>0)numeroLinea = new Integer(nroObtenido);
		    			lineaArchivo = FacturadorUtil.obtenerCodigoError(inputXSL,numeroLinea);
		    			if (lineaArchivo.length()>0)nroObtenido = FacturadorUtil.obtenerNumeroEnCadena(lineaArchivo);
						
		    			if("".equals(nroObtenido)){
							retorno = mensaje;
						}else{
							TxxxzBean txxxzBean = txxxzDAO.consultarErrorById(new Integer(nroObtenido));
							if(txxxzBean != null)
								mensajeError = txxxzBean.getNom_error();
							else
								mensajeError = mensaje;
							
							retorno = nroObtenido + " - " +  mensajeError;
						}
		    			fos.close();
					} catch (Exception ex) {
						log.error("Error Ejecucion de Cierre Archivo: " + ex.getMessage());
						retorno = ex.getMessage();
					}
		    }
		    
		    return retorno;
		    
	}
}
