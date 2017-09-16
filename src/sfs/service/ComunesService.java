package sfs.service;

import java.util.List;

public interface ComunesService {
	
	
	public String obtenerRutaTrabajo(String rutaTrabajoBusqueda) throws Exception;
	public void validarConexion(String direccion, Integer puerto) throws Exception;
	public Boolean validarVersionFacturador(String versionFacturador) throws Exception;			
	public Boolean actualizarVersionFacturador(String versionFacturador) throws Exception;
	public String leerEtiquetaArchivoXml(String rutaArchivo, String nombreEtiqueta) throws Exception;
	public List<String> leerEtiquetaListaArchivoXml(String rutaArchivo, String nombreEtiqueta) throws Exception;

}
