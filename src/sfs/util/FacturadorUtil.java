package sfs.util;
 
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;

import sfs.model.domain.TxxxxBean;
	
public class FacturadorUtil {
		
	public static String executeCommand(String command) throws Exception {

		StringBuffer output = new StringBuffer();

		Process p;
		BufferedReader reader = null;
		p = Runtime.getRuntime().exec(command);
		p.waitFor();
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = "";			
        while ((line = reader.readLine())!= null) {
        	output.append(line + "\n");
        }
			
		reader.close();

		return output.toString();

		}
		
		public static void comprimirArchivo( OutputStream salida, InputStream entrada, String nombre  ) throws Exception {
	    	byte[] buffer = new byte[1024];
	 
    		ZipOutputStream zos = new ZipOutputStream(salida);
    		ZipEntry ze= new ZipEntry(nombre);
    		zos.putNextEntry(ze);
 
    		int len;
    		while ((len = entrada.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
 
    		entrada.close();
    		zos.closeEntry();
 
    		zos.close();
		}
		
		public static String convertirListaJson(List<TxxxxBean> listaConvertir) throws Exception {
			
			StringBuilder strListado = new StringBuilder();
			
			if(listaConvertir.size() > 0 ){
				// Cuendo existen comprobantes que mostrar
				ObjectMapper mapper = new ObjectMapper();
				String lista = mapper.writeValueAsString(listaConvertir);
				strListado = new StringBuilder(lista);
			}else{
				// En caso no existan comprobantes que mostrar
				strListado.setLength(0);
				strListado.append("{").append("\"sEcho\": 1,")
									  .append("\"iTotalRecords\": 0,")
									  .append("\"iTotalDisplayRecords\": 0,")
									  .append("\"aaData\":[").append("]").append("}");
			}
			
			return strListado.toString();
		}
		
		public static String obtenerCodigoError(String rutaArchivo, Integer lineaArchivo) throws Exception{
			String linea = "";
			Integer contador=1;
			BufferedReader br = null; 
			try{
				br = new BufferedReader (new FileReader (rutaArchivo));
				while((linea = br.readLine())!= null){
					if(contador.intValue()==lineaArchivo.intValue())break;
					contador ++;
				} 
				br.close();
			}catch(Exception e){
				throw new Exception("Error en el utilitario obtenerLineaArchivo: " + e.getMessage());
			}
			
			if(linea == null)
				linea = "";

			return linea;
		}
		
		public static String obtenerNumeroEnCadena(String mensaje) throws Exception{
			Integer posicion = Integer.valueOf(mensaje.indexOf("codigo"));
		    if (posicion.intValue() > 0)
		    	mensaje = mensaje.substring(posicion.intValue());
		
			Integer largo = mensaje.length();
			String numero = "";
			for(int i=0; i <largo ; i++){ 
				if (Character.isDigit(mensaje.charAt(i))) 
					numero=numero+mensaje.charAt(i);
			} 
					
			return numero;
		}
		
		public static Boolean esNumerico(String cadena){
			Boolean retorno = cadena.matches("^[0-9]{1,2}$"); 
			return retorno;
		}
		
		public static String completarCeros(String cadena, String lado, Integer cantidad) {
			String cadenaCompletada = "";
			
			if("D".equals(lado))
				cadenaCompletada = String.format("%1$-" + cantidad + "s", cadena).replace(" ", "0");
			else
				cadenaCompletada = String.format("%1$" + cantidad + "s", cadena).replace(" ", "0");
			
			return cadenaCompletada;
		}
				
		public static void crearArchivoZip(String rutaArchivo, byte[] archivoZip) throws Exception{
			ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(archivoZip));
			ZipEntry entry = null;
			FileOutputStream out = null;
			while ((entry = zipStream.getNextEntry()) != null) {

			    String entryName = rutaArchivo + entry.getName();

			    out = new FileOutputStream(entryName);

			    byte[] byteBuff = new byte[4096];
			    int bytesRead = 0;
			    while ((bytesRead = zipStream.read(byteBuff)) != -1)
			    {
			        out.write(byteBuff, 0, bytesRead);
			    }

			    out.close();
			    zipStream.closeEntry();
			}
			zipStream.close();
			
		}
		
		public static void outputDocToOutputStream(Document doc, ByteArrayOutputStream signatureFile) throws TransformerException {
	        TransformerFactory factory	   	   = TransformerFactory.newInstance();
	        Transformer		   transformer	   = factory.newTransformer();
	        
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
	        //"ISO-8859-9"
	        transformer.transform(new DOMSource(doc), new StreamResult(signatureFile));		
		}	
		
		
		
	}