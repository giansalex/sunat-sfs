package sfs.service;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class ResourceResolver  implements LSResourceResolver {

	private static final Log log = LogFactory.getLog(ResourceResolver.class);
	
public LSInput resolveResource(String type, String namespaceURI,
        String publicId, String systemId, String baseURI) {

	String rutaArchivo = "";
	
	rutaArchivo = systemId.replace("../","");
	log.debug("rutaArchivo Primera Depuracion: " + rutaArchivo);
	
	if(rutaArchivo.indexOf("common/") == -1 )
		rutaArchivo = "common/" + systemId;
		
	log.debug("rutaArchivo Segunda Depuracion: " + rutaArchivo);
		
    InputStream resourceAsStream = this.getClass().getResourceAsStream(rutaArchivo);
    return new Input(publicId, rutaArchivo, resourceAsStream);
}

 }
