package sfs.service;

import java.util.List;
import java.util.Map;

import sfs.model.domain.TxxxxBean;
import sfs.model.domain.TxxxyBean;

public interface BandejaDocumentosService {
	
	public void eliminarBandeja(TxxxxBean txxxxBean) throws Exception;
	public List<TxxxxBean> consultarBandejaComprobantesPorId(TxxxxBean txxxxBean) throws Exception;
	public List<TxxxxBean> consultarBandejaComprobantes() throws Exception;
    public void cargarArchivosContribuyente() throws Exception;
    public String generarComprobantePagoSunat(TxxxxBean txxxxBean) throws Exception;
    public String listarCertificados(Map<String, Object> obj) throws Exception;
    public String importarCertificado(Map<String, Object> obj)throws Exception;
    public String obtenerParametro(Map<String, Object> obj) throws Exception;
    public String grabarParametro(Map<String, Object> obj) throws Exception;
    public List<TxxxxBean> buscarBandejaPorSituacion(String situacionComprobante) throws Exception;
    public void actualizarEstadoBandejaCdp(TxxxxBean txxxxBean) throws Exception;
    public List<TxxxyBean> consultarParametro(TxxxyBean txxxyBean) throws Exception;
    public Map<String,Object> enviarComprobantePagoSunat(TxxxxBean txxxxBean) throws Exception;
    public String grabarOtrosParametro(Map<String, Object> obj) throws Exception;
    public String obtenerOtrosParametro(Map<String, Object> obj) throws Exception;
    public String validarParametroRegistrado() throws Exception;
    public void cargarArchivoContribuyente() throws Exception;
    public void cargarArchivoContribuyente(String directorio, String nombreArchivo) throws Exception;
}