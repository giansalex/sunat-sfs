package sfs.service;

import java.util.HashMap;
import java.util.Map;

public interface GenerarDocumentosService {

	public String validaCertificado(String input, String numRuc, String passPrivateKey) throws Exception;
	public String validarXML(String tipoComprobante, String rutaEntrada, String nombreArchivo) throws Exception;
	public void formatoPlantillaXml(String tipoDocumento, String[] archivos, String nombreArchivo) throws Exception;
	public void convertirAXml(String rutaEntrada, String nombreArchivo)throws Exception;
	public String firmarComprimirXml(String nombreArchivo) throws Exception;
	public String Desencriptar(String textoEncriptado) throws Exception;
	public String Encriptar(String texto) throws Exception;
	public Map<String,String> enviarArchivoSunat(String wsUrl,String filename,String tipoComprobante) throws Exception;
	public void formatoJsonPlantilla(HashMap<String,Object> objectoJson, String nombreArchivo) throws Exception;
	public Map<String,String> obtenerEstadoTicket(String rutaArchivo, String wsUrl, String nroTicket) throws Exception;
	public void validarSchemaXML(String tipoComprobante, String nombreArchivo) throws Exception;
	public String obtenerArchivoXml(String nombreArchivo) throws Exception;
	public void adicionarInformacionFacturador(String nombreArchivoXml) throws Exception; 
}
