package sfs.ws;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

import static sfs.util.Constantes.CONSTANTE_DATA;
import static sfs.util.Constantes.CONSTANTE_FIRMA;
import static sfs.util.Constantes.CONSTANTE_SITUACION_XML_GENERADO;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_RBAJAS;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import sfs.model.domain.TxxxxBean;
import sfs.service.BandejaDocumentosService;
import sfs.service.ComunesService;
import sfs.util.FacturadorUtil;

@WebService
@Component
public class TransferirArchivoImpl extends SpringBeanAutowiringSupport implements TransferirArchivo {
	
	@Autowired
	private ComunesService comunesService;

	@Autowired
	private BandejaDocumentosService bandejaDocumentosService;
	
	private static final Log log = LogFactory.getLog(TransferirArchivoImpl.class);
	
	@PostConstruct
    public void postConstruct() {
        log.debug("postconstruct has run.");
        super.processInjectionBasedOnCurrentContext(this);
    }
	
	@Override
    public byte[] generarComprobante(String archivoEnvio, byte[] archivoZip) throws Exception{
        byte[] fileBytes = null;
        
        try {
        	String rutaArchData = comunesService.obtenerRutaTrabajo(CONSTANTE_DATA);
            String rutaArchFirm = comunesService.obtenerRutaTrabajo(CONSTANTE_FIRMA);
            String numRuc = "", tipoDocumento ="", nroDocumento ="";
        	FacturadorUtil.crearArchivoZip(rutaArchData, archivoZip);
        	bandejaDocumentosService.cargarArchivoContribuyente(rutaArchData, archivoEnvio);
        	String[] datoProceso = archivoEnvio.split("\\-");
        	
        	if(!CONSTANTE_TIPO_DOCUMENTO_RBAJAS.equals(datoProceso[1])){
				numRuc = datoProceso[0];
				tipoDocumento = datoProceso[1];
				nroDocumento = datoProceso[2]+"-"+datoProceso[3];
			}else{
				numRuc = datoProceso[0];
				tipoDocumento = datoProceso[1];
				nroDocumento = datoProceso[1]+"-"+datoProceso[2]+"-"+datoProceso[3];
			}
        	
        	TxxxxBean txxxxBean = null;
        	TxxxxBean txxxxBeanBusq = new TxxxxBean();
        	txxxxBeanBusq.setNum_ruc(numRuc);
        	txxxxBeanBusq.setTip_docu(tipoDocumento);
        	txxxxBeanBusq.setNum_docu(nroDocumento);
        	List<TxxxxBean> lista = bandejaDocumentosService.consultarBandejaComprobantesPorId(txxxxBeanBusq);
			// Generar Archivo XML
			if (lista.size() > 0){
				txxxxBean = lista.get(0);
				String resultado = bandejaDocumentosService.generarComprobantePagoSunat(txxxxBean);
				if("".equals(resultado)){
					txxxxBean.setFec_gene("FECHA_GENERACION");
					txxxxBean.setInd_situ(CONSTANTE_SITUACION_XML_GENERADO);
					txxxxBean.setDes_obse("-");
					bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBean);
					
		            File file = new File(rutaArchFirm, archivoEnvio+".xml");
		            FileInputStream fis = new FileInputStream(file);
		            BufferedInputStream inputStream = new BufferedInputStream(fis);
		            fileBytes = new byte[(int) file.length()];
		            inputStream.read(fileBytes);
		            inputStream.close();
					
				}else
					throw new Exception("El estado del Cdp no es POR GENERAR XML ó POR VALIDAR XML.");
			}
          
            return fileBytes;
             
        } catch (IOException ex) {
        	log.error("IOException: " + ex.getMessage() + " Causa: " + ex.getCause());
            throw new WebServiceException("IOException: " + ex.getMessage() + " Causa: " + ex.getCause());
        } catch (Exception e) {
        	log.error("IOException: " + e.getMessage() + " Causa: " + e.getCause());
            throw new WebServiceException("IOException: " + e.getMessage() + " Causa: " + e.getCause());
        } 
    } 
    
	public ComunesService getComunesService() {
		return comunesService;
	}

	public BandejaDocumentosService getBandejaDocumentosService() {
		return bandejaDocumentosService;
	}
	
	
	
}
