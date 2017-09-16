package sfs.job;

import static sfs.util.Constantes.CONSTANTE_RPTA;
import static sfs.util.Constantes.CONSTANTE_SITUACION_ENVIADO_POR_PROCESAR;
import static sfs.util.Constantes.CONSTANTE_SITUACION_ENVIADO_PROCESANDO;
import static sfs.util.Constantes.CONSTANTE_SITUACION_POR_GENERAR_XML;
import static sfs.util.Constantes.CONSTANTE_SITUACION_XML_GENERADO;
import static sfs.util.Constantes.CONSTANTE_SITUACION_CON_ERRORES;
import static sfs.util.Constantes.CONSTANTE_SITUACION_XML_VALIDAR;
import static sfs.util.Constantes.CONSTANTE_TIPO_FUNCION_AUTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import sfs.model.dao.TxxxyDAO;
import sfs.model.domain.TxxxxBean;
import sfs.model.domain.TxxxyBean;
import sfs.service.BandejaDocumentosBatchService;
import sfs.service.BandejaDocumentosService;
import sfs.service.ComunesService;

@Service
public class GestionarProcesosBatchJob {

	private static final Log log = LogFactory.getLog(GestionarProcesosBatchJob.class);

	@Autowired
	private BandejaDocumentosService bandejaDocumentosService;
	
	@Autowired
	private BandejaDocumentosBatchService bandejaDocumentosBatchService;
	
	@Autowired
	private ComunesService comunesService;
	
	@Autowired
	private TxxxyDAO txxxyDAO;

	@Scheduled(cron="${TiempoActualizaBajas}")
	@Transactional(propagation = Propagation.REQUIRED, isolation=Isolation.SERIALIZABLE)
	public void ActualizarBaja() {
		
		synchronized(this){
			
			List<TxxxxBean> listadoBandeja = null;
			Map<String,String> resultado = null; 
			String ticket = "", rutaArchivo=""; 

			try{
				listadoBandeja = bandejaDocumentosService.buscarBandejaPorSituacion(CONSTANTE_SITUACION_ENVIADO_POR_PROCESAR);
			}catch(Exception e){
				log.error("Error en buscarBandejaPorSituacion: " + e.getMessage() + " Causa: " + e.getCause());
			} 


			for(TxxxxBean txxxxBean:listadoBandeja){
				ticket = txxxxBean.getDes_obse();
				ticket = ticket.replace("Nro. Ticket: ","");
				try{
					
					rutaArchivo = comunesService.obtenerRutaTrabajo(CONSTANTE_RPTA);
					resultado = bandejaDocumentosBatchService.actualizarEstadoBaja(rutaArchivo, ticket);
					
					String situacion = resultado.get("situacion");
					String mensaje = resultado.get("mensaje");
					
					if(resultado != null){
						txxxxBean.setFec_envi("FECHA_ENVIO");
						txxxxBean.setInd_situ(situacion);
						txxxxBean.setDes_obse(mensaje);
					}				
					bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBean);
									
				}catch(Exception e){
					log.error(e.getMessage());
					txxxxBean.setFec_envi("FECHA_ENVIO");
					txxxxBean.setInd_situ(CONSTANTE_SITUACION_ENVIADO_PROCESANDO);
					txxxxBean.setDes_obse("Nro. Ticket: " + ticket);
					
					try {
						bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBean);
					} catch (Exception ex) {
						log.error("Error al actualizarEstadoBaja metodo actualizarEstadoBandejaCdp: " + ex.getMessage());
					}
				}
			}

			
		}
		
		
	}
	
	@Scheduled(cron="${TiempoGenComprobante}")
	@Transactional(propagation = Propagation.REQUIRED, isolation=Isolation.SERIALIZABLE)
	public void generarComprobante() {
		synchronized(this){
			
			List<TxxxxBean> listadoBandejaGenerar = null;
			List<TxxxxBean> listadoBandejaValidar = null;
			TxxxxBean txxxxBean = null;
			String digestValue = "";
			
			try{
				
				TxxxyBean txxxyBean = new TxxxyBean();
				txxxyBean.setId_para("PARASIST");
				txxxyBean.setCod_para("FUNCIO");
				List<TxxxyBean> listado = null;
				listado = txxxyDAO.consultarParametro(txxxyBean);
				if(CONSTANTE_TIPO_FUNCION_AUTO.equals(listado.get(0).getVal_para())){
					// Leyendo archivos del directorio del contribuyente
					bandejaDocumentosService.cargarArchivoContribuyente();
					
					listadoBandejaGenerar = bandejaDocumentosService.buscarBandejaPorSituacion(CONSTANTE_SITUACION_POR_GENERAR_XML);
					if((listadoBandejaGenerar!=null)&&(listadoBandejaGenerar.size())> 0){
						txxxxBean = listadoBandejaGenerar.get(0);
						// Generar Archivo XML
						String listaGenerarComprobantePago = bandejaDocumentosService.generarComprobantePagoSunat(txxxxBean);
						if("".equals(listaGenerarComprobantePago)){
							txxxxBean.setFec_gene("FECHA_GENERACION");	
							txxxxBean.setInd_situ(CONSTANTE_SITUACION_XML_GENERADO);
							txxxxBean.setFirm_digital("-");
							txxxxBean.setDes_obse("-");
							bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBean);
						}
					}
					
					listadoBandejaValidar = bandejaDocumentosService.buscarBandejaPorSituacion(CONSTANTE_SITUACION_XML_VALIDAR);
					if((listadoBandejaValidar!=null)&&(listadoBandejaValidar.size())> 0){
						txxxxBean = listadoBandejaValidar.get(0);
						// Generar Archivo XML
						String listaValidarComprobantePago = bandejaDocumentosService.generarComprobantePagoSunat(txxxxBean);
						if("".equals(listaValidarComprobantePago)){
							txxxxBean.setFec_gene("FECHA_GENERACION");	
							txxxxBean.setInd_situ(CONSTANTE_SITUACION_XML_GENERADO);
							txxxxBean.setFirm_digital("-");
							txxxxBean.setDes_obse("-");
							bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBean);
						}
					}
				}

			}catch(Exception e){
				String mensaje = e.getMessage();
				log.error(mensaje); 
				txxxxBean.setInd_situ(CONSTANTE_SITUACION_CON_ERRORES);
				txxxxBean.setFirm_digital(digestValue);
				txxxxBean.setDes_obse(e.getMessage());
				try {
					bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBean);
					// Buscando Todos los Comprobantes Cargados al Sistema
				} catch (Exception ex) {
					log.error(ex.getMessage());
				}
			}
			
		}
			
	}
	
	
	@Scheduled(cron="${TiempoEnvComprobante}")
	@Transactional(propagation = Propagation.REQUIRED, isolation=Isolation.SERIALIZABLE)
	public void enviarComprobante() {
		
		synchronized(this){
			List<TxxxxBean> listadoBandejaEnviar = null;
			TxxxxBean txxxxBean = null;
			String digestValue = "";
			try{
				/* Consulta Parametro de Timer Envio */
				TxxxyBean txxxyBean = new TxxxyBean();
				txxxyBean.setId_para("PARASIST");
				txxxyBean.setCod_para("TIMEENVIA");
				List<TxxxyBean> listado = bandejaDocumentosService.consultarParametro(txxxyBean);
				if(listado.size() > 0&&!"".equals(listado.get(0).getVal_para())){
					listadoBandejaEnviar = bandejaDocumentosService.buscarBandejaPorSituacion(CONSTANTE_SITUACION_XML_GENERADO);
					if(listadoBandejaEnviar.size() > 0){
						txxxxBean = listadoBandejaEnviar.get(0);
						// Enviar Archivo XML
						Map<String,Object> envioBandeja = bandejaDocumentosService.enviarComprobantePagoSunat(txxxxBean);
						@SuppressWarnings("unchecked")
						Map<String,String> estadoRetorno = envioBandeja.get("resultadoWebService")!=null?(HashMap<String,String>)envioBandeja.get("resultadoWebService"):new HashMap<String,String>();
						if(estadoRetorno!=null){
							digestValue = envioBandeja.get("digestValue")!=null?(String)envioBandeja.get("digestValue"):"";
							String situacion = estadoRetorno.get("situacion")!=null?(String)estadoRetorno.get("situacion"):CONSTANTE_SITUACION_CON_ERRORES;
							String mensaje = estadoRetorno.get("mensaje")!=null?(String)estadoRetorno.get("mensaje"):"-";
							if(!"".equals(estadoRetorno)){
								txxxxBean.setFec_envi("FECHA_ENVIO");
								txxxxBean.setInd_situ(situacion);
								txxxxBean.setFirm_digital(digestValue);
								txxxxBean.setDes_obse(mensaje);
								bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBean);
							}
						}
					}
				}
			}catch(Exception e){
				String mensaje = e.getMessage();
				log.error(mensaje); 
				txxxxBean.setInd_situ(CONSTANTE_SITUACION_CON_ERRORES);
				txxxxBean.setFirm_digital(digestValue);
				txxxxBean.setDes_obse(e.getMessage());
				try {
					bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBean);
					// Buscando Todos los Comprobantes Cargados al Sistema
				} catch (Exception ex) {
					log.error(ex.getMessage());
				}
			}
		}
		
	}

	
}
