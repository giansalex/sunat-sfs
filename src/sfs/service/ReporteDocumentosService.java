package sfs.service;

public interface ReporteDocumentosService {
	
	public void imprimirComprobante(String nombreReporteJrxml, String nombreArchivoSalida, String nombreArchivoXml, String rutaBaseXml, String nombreArchivo) throws Exception;

}
