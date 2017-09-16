package sfs.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface TransferirArchivo {
	@WebMethod
    public byte[] generarComprobante(String archivoEnvio, byte[] archivoZip) throws Exception;    
}
