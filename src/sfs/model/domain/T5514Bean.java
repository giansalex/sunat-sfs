package sfs.model.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Clase que permite obtener los parametros de la tabla t5514sst_aplic
 * @author SOFT & NET SOLUTIONS
 */
public class T5514Bean implements Serializable {
	private static final long serialVersionUID = -4290535520422585611L;

	private Integer cod_st;				//Código de sello de tiempo

    private Integer cod_aplic;			//Código de aplicación

    private Date fec_regis;				//Fecha de registro

    private String cod_usuregis;		//Usuario de Registro

    private Date fec_modif;				//Fecha de modificación

    private String cod_usumodif;		//Usuario de modificación

    /**
   	 * Obtiene el valor de código de sello de tiempo
   	 * @author SOFT & NET SOLUTIONS
   	 * @return Integer cod_st código de sello de tiempo
   	 */
    public Integer getCod_st() {
        return cod_st;
    }

    /**
   	 * Reemplaza el valor de código de sello de tiempo
   	 * @author SOFT & NET SOLUTIONS
   	 * @param Integer cod_st código de sello de tiempo
   	 */
    public void setCod_st(Integer cod_st) {
        this.cod_st = cod_st;
    }

    /**
   	 * Obtiene el valor de código de aplicación
   	 * @author SOFT & NET SOLUTIONS
   	 * @return Integer cod_aplic código de aplicación
   	 */
    public Integer getCod_aplic() {
        return cod_aplic;
    }

    /**
   	 * Reemplaza el valor de código de aplicación
   	 * @author SOFT & NET SOLUTIONS
   	 * @param Integer cod_aplic código de aplicación
   	 */
    public void setCod_aplic(Integer cod_aplic) {
        this.cod_aplic = cod_aplic;
    }

    /**
   	 * Obtiene el valor de la fecha de registro
   	 * @author SOFT & NET SOLUTIONS
   	 * @return Date fec_regis fecha de registro
   	 */
   	public Date getFec_regis() {
   		return fec_regis;
   	}

   	/**
   	 * Reemplaza el valor de la fecha de registro
   	 * @author SOFT & NET SOLUTIONS
   	 * @param Date fec_regis fecha de registro
   	 */
   	public void setFec_regis(Date fec_regis) {
   		this.fec_regis = fec_regis;
   	}

   	/**
   	 * Obtiene el valor de usuario de registro
   	 * @author SOFT & NET SOLUTIONS
   	 * @return String cod_usuregis usuario de registro
   	 */
   	public String getCod_usuregis() {
   		return cod_usuregis;
   	}

   	/**
   	 * Reemplaza el valor de usuario de registro
   	 * @author SOFT & NET SOLUTIONS
   	 * @param String cod_usuregis usuario de registro
   	 */
   	public void setCod_usuregis(String cod_usuregis) {
   		this.cod_usuregis = cod_usuregis;
   	}

   	/**
   	 * Obtiene el valor de fecha de modificación
   	 * @author SOFT & NET SOLUTIONS
   	 * @return Date fec_modif fecha de modificación
   	 */
   	public Date getFec_modif() {
   		return fec_modif;
   	}

   	/**
   	 * Reemplaza el valor de fecha de modificación
   	 * @author SOFT & NET SOLUTIONS
   	 * @param Date fec_modif fecha de modificación
   	 */
   	public void setFec_modif(Date fec_modif) {
   		this.fec_modif = fec_modif;
   	}

   	/**
   	 * Obtiene el valor del usuario de modificación
   	 * @author SOFT & NET SOLUTIONS
   	 * @param String cod_usumodif código usuario modificación
   	 */
   	public String getCod_usumodif() {
   		return cod_usumodif;
   	}

   	/**
   	 * Reemplaza el valor del usuario de modificación
   	 * @author SOFT & NET SOLUTIONS
   	 * @param String cod_usumodif código usuario modificación
   	 */
   	public void setCod_usumodif(String cod_usumodif) {
   		this.cod_usumodif = cod_usumodif;
   	}

}