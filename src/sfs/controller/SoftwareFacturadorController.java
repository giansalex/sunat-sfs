package sfs.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

// Spring
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import static sfs.util.Constantes.CONSTANTE_SITUACION_ENVIADO_ACEPTADO;
import static sfs.util.Constantes.CONSTANTE_SITUACION_ENVIADO_ACEPTADO_OBSERVACIONES;
import static sfs.util.Constantes.CONSTANTE_SITUACION_XML_GENERADO;
import static sfs.util.Constantes.CONSTANTE_SITUACION_CON_ERRORES;
import static sfs.util.Constantes.CONSTANTE_FORMATO;
import static sfs.util.Constantes.CONSTANTE_REPO;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_FACTURA;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_BOLETA;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_NCREDITO;
import static sfs.util.Constantes.CONSTANTE_TIPO_DOCUMENTO_NDEBITO;
import static sfs.util.Constantes.CONSTANTE_ORIDAT;
import static sfs.util.Constantes.CONSTANTE_VERSION_SFS;
import static sfs.util.Constantes.CONSTANTE_TIPO_FUNCION_AUTO;
import sfs.model.domain.TxxxxBean;
import sfs.model.domain.TxxxyBean;
import sfs.service.BandejaDocumentosService;
import sfs.service.ComunesService;
import sfs.service.ReporteDocumentosService;
import sfs.util.FacturadorUtil;

@Controller
public class SoftwareFacturadorController {

	private static final Log log = LogFactory.getLog(SoftwareFacturadorController.class);
				
	@Autowired
	private BandejaDocumentosService bandejaDocumentosService;
	

	@Autowired
	private ReporteDocumentosService reporteDocumentosService;
	
	@Autowired
	private ComunesService comunesService;
	
				
	@RequestMapping(value="/index")
	public ModelAndView iniciarProceso(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redir) throws Exception{
		ModelAndView modelAndView = null;
		String funcionalidad="", numRuc="", razonSocial="", tiempoTemporizador="", mensajeError = "";
		String listaBandeja="", mensajeValidacion = "", mensaje="";
		try{

			/* En caso se trate de una actualización de versión ó de validación de parámetros */
			Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);
			if (flashMap != null)
				mensaje = (String)flashMap.get("mensaje");
			
			mensajeValidacion = bandejaDocumentosService.validarParametroRegistrado();
			
			Map<String,String> resultado = null;
			if("".equals(mensajeValidacion)){
				// Leyendo archivos del directorio del contribuyente
				TxxxyBean txxxyBean = new TxxxyBean();
				txxxyBean.setId_para("PARASIST");
				txxxyBean.setCod_para("FUNCIO");
				List<TxxxyBean> parametroSistema = bandejaDocumentosService.consultarParametro(txxxyBean);
				if(!CONSTANTE_TIPO_FUNCION_AUTO.equals(parametroSistema.get(0).getVal_para()))
					bandejaDocumentosService.cargarArchivosContribuyente();
				
				resultado = consultarBandejaFacturador();
				mensajeError = "\"SN\"";
				
			}else{
				/* En caso no se haya registrado algún parámetro */
				StringBuilder strListado = new StringBuilder(); 
				strListado.setLength(0);
				strListado.append("{").append("\"sEcho\": 1,")
									  .append("\"iTotalRecords\": 0,")
									  .append("\"iTotalDisplayRecords\": 0,")
									  .append("\"aaData\":[").append("]").append("}");
				
				resultado = new HashMap<String,String>();
				resultado.put("listaBandeja",strListado.toString());
				mensajeError = "\"" + mensajeValidacion + "\""; 
			}
			
		    if(!"".equals(mensaje)){
				if("NOREQUIERE".equals(mensaje))
					mensajeError = "\"La aplicación ya se encuentra actualizada.\"";
				else
					mensajeError = "\"La aplicación se acaba de actualizar correctamente.\"";
			}
		
				
			// Configurando parametros
			funcionalidad = resultado.get("funcionalidad")!=null&&!"".equals(resultado.get("funcionalidad"))?resultado.get("funcionalidad"):"02";
			numRuc = resultado.get("numRuc")!=null&&!"".equals(resultado.get("numRuc"))?resultado.get("numRuc"):"00000000000";
			razonSocial = resultado.get("razonSocial")!=null&&!"".equals(resultado.get("razonSocial"))?resultado.get("razonSocial"):"NOMBRE DEL CONTRIBUYENTE";
			tiempoTemporizador = resultado.get("tiempoTemporizador")!=null&&!"".equals(resultado.get("tiempoTemporizador"))?resultado.get("tiempoTemporizador"):"0";
			listaBandeja = resultado.get("listaBandeja")!=null&&!"".equals(resultado.get("listaBandeja"))?resultado.get("listaBandeja"):"";
			
			
			modelAndView = new ModelAndView("bandejaFacturador");
			modelAndView.addObject("listaBandejaFacturador", listaBandeja);
			modelAndView.addObject("tipoFuncionalidad", funcionalidad);
			modelAndView.addObject("numRuc", numRuc);
			modelAndView.addObject("razonSocial", razonSocial);
			modelAndView.addObject("mensajeError", mensajeError);
			modelAndView.addObject("tiempoTemporizador", tiempoTemporizador);
			modelAndView.addObject("version", CONSTANTE_VERSION_SFS);
					
											
		}catch(Exception e){
			log.error("SoftwareFacturadorController.consultarBandejaFacturador...Error Ejecucion: " + e.getMessage() + " Causa: " + e.getCause());
			// Buscando Todos los Comprobantes Cargados al Sistema
			Map<String,String> resultado = consultarBandejaFacturador();
			
			// Configurando parametros
			funcionalidad = resultado.get("funcionalidad")!=null&&!"".equals(resultado.get("funcionalidad"))?resultado.get("funcionalidad"):"02";
			numRuc = resultado.get("numRuc")!=null&&!"".equals(resultado.get("numRuc"))?resultado.get("numRuc"):"00000000000";
			razonSocial = resultado.get("razonSocial")!=null&&!"".equals(resultado.get("razonSocial"))?resultado.get("razonSocial"):"NOMBRE DEL CONTRIBUYENTE";
			tiempoTemporizador = resultado.get("tiempoTemporizador")!=null&&!"".equals(resultado.get("tiempoTemporizador"))?resultado.get("tiempoTemporizador"):"0";
			listaBandeja = resultado.get("listaBandeja")!=null&&!"".equals(resultado.get("listaBandeja"))?resultado.get("listaBandeja"):"";
			mensajeError = "\"SN\"";
			
			modelAndView = new ModelAndView("bandejaFacturador");
			modelAndView.addObject("listaBandejaFacturador", listaBandeja);
			modelAndView.addObject("tipoFuncionalidad", funcionalidad);
			modelAndView.addObject("numRuc", numRuc);
			modelAndView.addObject("razonSocial", razonSocial);
			modelAndView.addObject("mensajeError", mensajeError);
			modelAndView.addObject("tiempoTemporizador", tiempoTemporizador);
			modelAndView.addObject("version", CONSTANTE_VERSION_SFS);

		}
								
		
		return modelAndView;
		
	}
	
	
	@RequestMapping(value="/generarXml", method = RequestMethod.POST)
	public ModelAndView generarXml(HttpServletRequest request, RedirectAttributes redir) throws Exception {
		String mensajeValidacion = bandejaDocumentosService.validarParametroRegistrado();		
		
		ModelAndView modelAndView = new ModelAndView("redirect:/index.htm");
		redir.addFlashAttribute("mensaje",mensajeValidacion);
		
		if("".equals(mensajeValidacion)){
			TxxxxBean txxxxBean = new TxxxxBean();
			TxxxxBean txxxxBeanResp = null; 
			try
			{
				// Informacion de busqueda del comprobante
				String numRuc = request.getParameter("hddNumRuc");
				String tipDoc = request.getParameter("hddTipDoc");
				String numDoc = request.getParameter("hddNumDoc");
				// Cargando Bean de Busqueda
				txxxxBean.setNum_ruc(numRuc);
				txxxxBean.setTip_docu(tipDoc);
				txxxxBean.setNum_docu(numDoc);
				List<TxxxxBean> lista = bandejaDocumentosService.consultarBandejaComprobantesPorId(txxxxBean);
				// Generar Archivo XML
				if (lista.size() > 0){
					txxxxBeanResp = lista.get(0);
					String resultado = bandejaDocumentosService.generarComprobantePagoSunat(txxxxBeanResp);
					if("".equals(resultado)){
						txxxxBeanResp.setFec_gene("FECHA_GENERACION");
						txxxxBeanResp.setInd_situ(CONSTANTE_SITUACION_XML_GENERADO);
						txxxxBeanResp.setDes_obse("-");
						bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBeanResp);
					}
					
				}else{
					throw new Exception("No existen datos que procesar.");
				}		
			}catch(Exception e){
				log.error(e) ; 
				txxxxBeanResp.setInd_situ(CONSTANTE_SITUACION_CON_ERRORES);
				txxxxBeanResp.setDes_obse(e.getMessage());
				bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBeanResp);
			}
		}
		
		return modelAndView;
	}
		
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/enviarXML", method = RequestMethod.POST)
	public ModelAndView enviarXML(HttpServletRequest request, RedirectAttributes redir) throws Exception {
		
		String mensajeValidacion = bandejaDocumentosService.validarParametroRegistrado();
		ModelAndView modelAndView = new ModelAndView("redirect:/index.htm");
		redir.addFlashAttribute("mensaje",mensajeValidacion);
		
		if("".equals(mensajeValidacion)){
		
			TxxxxBean txxxxBean = new TxxxxBean();
			TxxxxBean txxxxBeanResp = null; 
			try
			{
				// Informacion de busqueda del comprobante
				String numRuc = request.getParameter("hddNumRuc");
				String tipDoc = request.getParameter("hddTipDoc");
				String numDoc = request.getParameter("hddNumDoc");
				// Cargando Bean de Busqueda
				txxxxBean.setNum_ruc(numRuc);
				txxxxBean.setTip_docu(tipDoc);
				txxxxBean.setNum_docu(numDoc);
				List<TxxxxBean> lista = bandejaDocumentosService.consultarBandejaComprobantesPorId(txxxxBean);
				// Generar Archivo XML
				if (lista.size() > 0){
					txxxxBeanResp = lista.get(0);
					Map<String,Object> envioBandeja = bandejaDocumentosService.enviarComprobantePagoSunat(txxxxBeanResp);
					if(envioBandeja!=null){
						Map<String,String> resultadoWebService = (HashMap<String,String>)envioBandeja.get("resultadoWebService");
						String estadoRetorno = resultadoWebService.get("situacion")!=null?(String)resultadoWebService.get("situacion"):"";
						String mensaje = resultadoWebService.get("mensaje")!=null?(String)resultadoWebService.get("mensaje"):"-";
						if(!"".equals(estadoRetorno)){
							txxxxBeanResp.setFec_envi("FECHA_ENVIO");
							txxxxBeanResp.setInd_situ(estadoRetorno);
							txxxxBeanResp.setDes_obse(mensaje);	
							bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBeanResp);
						}
					}
					
				}else{
					throw new Exception("No existen datos que procesar.");
				}		
			}catch(Exception e){
				String mensaje = "Hubo un problema al invocar servicio SUNAT: " + e.getMessage();
				log.error(mensaje); 
				txxxxBeanResp.setInd_situ(CONSTANTE_SITUACION_CON_ERRORES);
				txxxxBeanResp.setDes_obse(mensaje);
				bandejaDocumentosService.actualizarEstadoBandejaCdp(txxxxBeanResp);
	
			}
		}					
		
		return modelAndView; 
		
	}
	
	
	@RequestMapping(value = "/MostrarXml", method = RequestMethod.POST, headers = { "Content-type=application/json" })
	public @ResponseBody String mostrarXml(HttpServletRequest request,@RequestBody Map<String, Object> obj){
		String retorno = "";
		
		String nomArch = obj.get("nomArch")!=null?(String)obj.get("nomArch"):"";
		String sitArch = obj.get("sitArch")!=null?(String)obj.get("sitArch"):"";
						
		try{
			
			if((!CONSTANTE_SITUACION_ENVIADO_ACEPTADO.equals(sitArch))&&(!CONSTANTE_SITUACION_ENVIADO_ACEPTADO_OBSERVACIONES.equals(sitArch)))
				throw new Exception("El comprobante de pago no se encuentra en estado Aceptado/Aceptado con Observaciones por SUNAT");
						
			String formatoArchivo[] = nomArch.split("\\-");
			String archivoJrxml = "";
			String patronXPath = "";
			if(CONSTANTE_TIPO_DOCUMENTO_FACTURA.equals(formatoArchivo[1])){
				archivoJrxml = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO) + "Plantilla_reporte_factura.jasper";
				patronXPath = "/Invoice/InvoiceLine";	
			}
			
			if(CONSTANTE_TIPO_DOCUMENTO_BOLETA.equals(formatoArchivo[1])){
				archivoJrxml = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO) + "Plantilla_reporte_boleta.jasper";
				patronXPath = "/Invoice/InvoiceLine";
			}
			
			if(CONSTANTE_TIPO_DOCUMENTO_NCREDITO.equals(formatoArchivo[1])){
				archivoJrxml = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO) + "Plantilla_reporte_notacredito.jasper";
				patronXPath = "/CreditNote/CreditNoteLine";
			}
			
			if(CONSTANTE_TIPO_DOCUMENTO_NDEBITO.equals(formatoArchivo[1])){
				archivoJrxml = comunesService.obtenerRutaTrabajo(CONSTANTE_FORMATO) + "Plantilla_reporte_notadebito.jasper";
				patronXPath = "/DebitNote/DebitNoteLine";
			}
			
			String reporteSalida = comunesService.obtenerRutaTrabajo(CONSTANTE_REPO) +  nomArch + ".pdf";
			String xmlOrigenDatos = comunesService.obtenerRutaTrabajo(CONSTANTE_ORIDAT) +  nomArch + ".xml"; 
						
			reporteDocumentosService.imprimirComprobante(archivoJrxml,reporteSalida,xmlOrigenDatos,patronXPath, nomArch);
			
			retorno = "Se acaba de crear el archivo " + nomArch + ".pdf, para consultarlo vaya a la ruta: "+comunesService.obtenerRutaTrabajo(CONSTANTE_REPO);
			
		}catch(Exception e){
			log.error(e.getMessage());
			retorno = "Validacion: " + e.getMessage();
		}
		
		return retorno;
		
	}
	
	
	@RequestMapping(value = "/GrabarParametro", method = RequestMethod.POST, headers = { "Content-type=application/json" })
	public @ResponseBody String grabarParametro(HttpServletRequest request,@RequestBody Map<String, Object> obj){
		String retorno = "";
		
		try{
			retorno = bandejaDocumentosService.grabarParametro(obj);
		}catch(Exception e){
			log.error(e.getMessage());			
		}
		
		return retorno;
		
	}
	
	
	@RequestMapping(value = "/CargarDatosParametro", method = RequestMethod.POST, headers = { "Content-type=application/json" })
	public @ResponseBody String obtenerParametro(HttpServletRequest request,@RequestBody Map<String, Object> obj){
		String retorno = "";
		try{
			retorno = bandejaDocumentosService.obtenerParametro(obj);
		}catch(Exception e){
			log.error(e.getMessage());			
		}
		
		return retorno;
		
	}
	
	@RequestMapping(value = "/CargarOtrosDatosParametro", method = RequestMethod.POST, headers = { "Content-type=application/json" })
	public @ResponseBody String obtenerOtrosParametro(HttpServletRequest request,@RequestBody Map<String, Object> obj){
		String retorno = "";
		try{
			retorno = bandejaDocumentosService.obtenerOtrosParametro(obj);
		}catch(Exception e){
			log.error(e.getMessage());			
		}
		
		return retorno;
		
	}
	
	@RequestMapping(value = "/GrabarOtrosParametro", method = RequestMethod.POST, headers = { "Content-type=application/json" })
	public @ResponseBody String GrabarOtrosParametro(HttpServletRequest request,@RequestBody Map<String, Object> obj){
		String retorno = "";
		
		try{
			retorno = bandejaDocumentosService.grabarOtrosParametro(obj);
		}catch(Exception e){
			log.error(e.getMessage());			
		}
		
		return retorno;
		
	}
	
	@RequestMapping(value = "/ImportarCertificado", method = RequestMethod.POST, headers = { "Content-type=application/json" })
	public @ResponseBody String importarCertificado(HttpServletRequest request,@RequestBody Map<String, Object> obj){
		String retorno = "";
		try{
			retorno = bandejaDocumentosService.importarCertificado(obj);
		}catch(Exception e){
			log.error(e.getMessage());			
		}
						
		return retorno;
	}
	
	@RequestMapping(value = "/CargarListaCertificado", method = RequestMethod.POST, headers = { "Content-type=application/json" })
	public @ResponseBody String cargarListaCertificado(HttpServletRequest request,@RequestBody Map<String, Object> obj){
		String retorno = "";
		try{
			retorno = bandejaDocumentosService.listarCertificados(obj);
		}catch(Exception e){
			log.error(e.getMessage());			
		}
						
		return retorno;
	}
	
	
	@RequestMapping(value = "/EliminarBandeja", method = RequestMethod.POST, headers = { "Content-type=application/json" })
	public @ResponseBody String eliminarBandeja(HttpServletRequest request,@RequestBody Map<String, Object> obj){
		String retorno = "";
		Map<String,Object> resultado = new HashMap<String,Object>();
		try{
			
			// Leyendo archivos del directorio del contribuyente
			TxxxxBean txxxxBean = new TxxxxBean();
			bandejaDocumentosService.eliminarBandeja(txxxxBean);
			
			// Actualizando hora de ejecucion
			resultado.put("validacion","EXITO");
			
		}catch(Exception e){
			resultado.put("validacion",e.getMessage());
			log.error(e.getMessage()); 
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			retorno = mapper.writeValueAsString(resultado);
		} catch (Exception e) {
			resultado.put("validacion",e.getMessage());
			log.error(e.getMessage());
		}
		
		return retorno;
	
	}
	
	
	@RequestMapping(value="/ActualizarFacturador")
	public ModelAndView actualizarFacturador(HttpServletRequest request, RedirectAttributes redir) throws Exception{
		String mensajeValidacion = "";
		ModelAndView modelAndView = null;
		try{
			Boolean resultado = comunesService.actualizarVersionFacturador(CONSTANTE_VERSION_SFS);
			
			 // Validar Version Actualizada
			if(resultado){
				mensajeValidacion = "NOREQUIERE";
			}else{
				// Buscando Todos los Comprobantes Cargados al Sistema
				mensajeValidacion = "FINALIZO";
			}
			
			modelAndView = new ModelAndView("redirect:/index.htm");
			redir.addFlashAttribute("mensaje",mensajeValidacion);
			
		}catch(Exception e){
			
			throw new Exception("Error al actualizar versión del facturador");
		}
		
		return modelAndView;
		
	}
	
	
	private Map<String,String> consultarBandejaFacturador() throws Exception{
		Map<String,String> retorno = new HashMap<String,String>();
		
		String listaBandejaFacturador = "";
		String tipoFuncionalidad = "";
		String numRuc = "";
		String nombreContribuyente = "";
		String tiempo = "";
	
		// Parametro de Funcionalidad Manual o Automática
		List<TxxxyBean> listado = new ArrayList<TxxxyBean>(); 
		TxxxyBean txxxyBean = new TxxxyBean();
		txxxyBean.setId_para("PARASIST");
		txxxyBean.setCod_para("FUNCIO");
		listado = bandejaDocumentosService.consultarParametro(txxxyBean);

		if( listado.size() > 0)
			tipoFuncionalidad = listado.get(0).getVal_para();
		
		
		// Parametro de Funcionalidad Manual o Automática
		listado = new ArrayList<TxxxyBean>(); 
		txxxyBean = new TxxxyBean();
		txxxyBean.setId_para("PARASIST");
		txxxyBean.setCod_para("NUMRUC");
		listado = bandejaDocumentosService.consultarParametro(txxxyBean);

		if( listado.size() > 0)
			numRuc = listado.get(0).getVal_para();
		
		// Parametro de Funcionalidad Manual o Automática
		listado = new ArrayList<TxxxyBean>(); 
		txxxyBean = new TxxxyBean();
		txxxyBean.setId_para("PARASIST");
		txxxyBean.setCod_para("RAZON");
		listado = bandejaDocumentosService.consultarParametro(txxxyBean);

		if( listado.size() > 0)
			nombreContribuyente = listado.get(0).getVal_para();
		
		// Parametro de Funcionalidad Manual o Automática
		listado = new ArrayList<TxxxyBean>(); 
		txxxyBean = new TxxxyBean();
		txxxyBean.setId_para("PARASIST");
		txxxyBean.setCod_para("TIEMPO");
		listado = bandejaDocumentosService.consultarParametro(txxxyBean);

		if( listado.size() > 0)
			tiempo = !"".equals(listado.get(0).getVal_para())?listado.get(0).getVal_para():"\"SN\"";
				
		// Buscando Todos los Comprobantes Cargados al Sistema
		List<TxxxxBean> listadoBandeja = bandejaDocumentosService.consultarBandejaComprobantes();
		listaBandejaFacturador = FacturadorUtil.convertirListaJson(listadoBandeja);
		
		retorno.put("listaBandeja", listaBandejaFacturador);
		retorno.put("funcionalidad", tipoFuncionalidad);
		retorno.put("numRuc", numRuc);
		retorno.put("razonSocial", nombreContribuyente);
		retorno.put("tiempoTemporizador", tiempo);
		
		
		return retorno;
		
	}	
	
	
	@RequestMapping(value = "/ActualizarPantalla", method = RequestMethod.POST, headers = { "Content-type=application/json" })
	public @ResponseBody String actualizarPantalla(HttpServletRequest request,@RequestBody Map<String, Object> obj){
		String retorno = "", temporal="";
		
		synchronized(this){
			
			Map<String,Object> resultado = new HashMap<String,Object>();
			List<TxxxxBean> listadoBandeja = null;
			try{
				
				// Leyendo archivos del directorio del contribuyente
				String mensajeValidacion = bandejaDocumentosService.validarParametroRegistrado();
				if("".equals(mensajeValidacion))
					listadoBandeja = bandejaDocumentosService.consultarBandejaComprobantes();
				
			}catch(Exception e){
				String mensaje = e.getMessage();
				log.error(mensaje); 
			}finally{
				// Actualizando hora de ejecucion
				SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				String fechaActual = format.format(new Date());
	
				// Actualizando hora de ejecucion
				resultado.put("validacion","EXITO");
				resultado.put("listaBandeja",listadoBandeja);
				resultado.put("fechaHora",fechaActual);
				
				ObjectMapper mapper = new ObjectMapper();
				try {
					temporal = mapper.writeValueAsString(resultado);
					retorno = new String(temporal.getBytes("UTF8"),"ISO8859_1");
				} catch (Exception e) {
					resultado.put("validacion","FALLO");
					log.error(e.getMessage());
				}
			}
		}
		
		return retorno;

	
	}	
	
}	
