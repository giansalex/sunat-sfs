package sfs.model.dao.spring;


import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import sfs.model.dao.TxxxxDAO;
import sfs.model.domain.TxxxxBean;

public class TxxxxDAOImpl implements TxxxxDAO {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = -7620828220507189880L;
	
	private final String findBandejaFacturadorTodo = "SELECT NUM_RUC,TIP_DOCU,NUM_DOCU,FEC_CARG,FEC_GENE,FEC_ENVI,DES_OBSE,NOM_ARCH,IND_SITU,TIP_ARCH,FIRM_DIGITAL FROM TXXXX_BANDFACT ORDER BY 6 DESC,5 DESC";
	private final String findBandejaFacturadorSitu = "SELECT NUM_RUC,TIP_DOCU,NUM_DOCU,FEC_CARG,FEC_GENE,FEC_ENVI,DES_OBSE,NOM_ARCH,IND_SITU,TIP_ARCH,FIRM_DIGITAL FROM TXXXX_BANDFACT WHERE IND_SITU = ? LIMIT 1";
	private final String findBandejaFacturadorById = "SELECT NUM_RUC,TIP_DOCU,NUM_DOCU,FEC_CARG,FEC_GENE,FEC_ENVI,DES_OBSE,NOM_ARCH,IND_SITU,TIP_ARCH,FIRM_DIGITAL FROM TXXXX_BANDFACT WHERE NUM_RUC = ? AND TIP_DOCU = ? AND NUM_DOCU = ?";
	private final String inseBandejaFacturadorTodo = "INSERT INTO TXXXX_BANDFACT (NUM_RUC,TIP_DOCU,NUM_DOCU,FEC_CARG,FEC_GENE,FEC_ENVI,DES_OBSE,NOM_ARCH,IND_SITU,TIP_ARCH,FIRM_DIGITAL) VALUES (?,?,?,strftime('%d/%m/%Y',?),strftime('%d/%m/%Y',?),strftime('%d/%m/%Y',?),?,?,?,?,'-')";
	private final String findBandejaFacturadorArch = "SELECT COUNT(1) FROM TXXXX_BANDFACT WHERE NOM_ARCH = ?";
	private final String updaBandejaFacturadorArch = "UPDATE TXXXX_BANDFACT SET IND_SITU = ?, DES_OBSE = ?  WHERE NOM_ARCH = ?";
	private final String updaBandejaFacturadorGene = "UPDATE TXXXX_BANDFACT SET IND_SITU = ?, DES_OBSE = ?, FEC_GENE = strftime('%d/%m/%Y %H:%M:%S',?), FEC_ENVI = strftime('%d/%m/%Y %H:%M','-') WHERE NOM_ARCH = ?";
	private final String updaBandejaFacturadorEnvi = "UPDATE TXXXX_BANDFACT SET IND_SITU = ?, DES_OBSE = ?, FEC_ENVI = strftime('%d/%m/%Y %H:%M:%S',?) WHERE NOM_ARCH = ?";
	private final String deleBandejaFacturadorTodo = "DELETE FROM TXXXX_BANDFACT";

	private JdbcTemplate jdbcTemplate;	
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public void eliminarBandeja(TxxxxBean txxxxBean){
		
		this.jdbcTemplate.update(this.deleBandejaFacturadorTodo);
				
	}
	
	
	@Override
	public List<TxxxxBean> consultarBandejaPorId(TxxxxBean txxxxBean) throws Exception{
		
		List<TxxxxBean> lista = new ArrayList<TxxxxBean>(); 
		List<Map<String, Object>>listaRetorno = jdbcTemplate.queryForList(this.findBandejaFacturadorById,txxxxBean.getNum_ruc(),txxxxBean.getTip_docu(),txxxxBean.getNum_docu());
		
		String fecCarg = null;
		String fecGene = null;
		String fecEnvi = null;
		TxxxxBean bean = null;
		for (Map<String,Object> row : listaRetorno) {
			fecCarg = row.get("FEC_CARG")!=null?(String)row.get("FEC_CARG"):"-";
			fecGene = row.get("FEC_GENE")!=null?(String)row.get("FEC_GENE"):"-";
			fecEnvi = row.get("FEC_ENVI")!=null?(String)row.get("FEC_ENVI"):"-";
			bean = new TxxxxBean();
			bean.setNum_ruc((String)row.get("NUM_RUC"));
    		bean.setTip_docu((String)row.get("TIP_DOCU"));
    		bean.setNum_docu((String)row.get("NUM_DOCU"));
    		bean.setFec_carg(fecCarg);
    		bean.setFec_gene(fecGene);
    		bean.setFec_envi(fecEnvi);
    		bean.setDes_obse((String)row.get("DES_OBSE"));
    		bean.setNom_arch((String)row.get("NOM_ARCH"));
    		bean.setInd_situ((String)row.get("IND_SITU"));
    		bean.setTip_arch((String)row.get("TIP_ARCH"));
    		bean.setNum_ruc((String)row.get("NUM_RUC"));
    		bean.setFirm_digital((String)row.get("FIRM_DIGITAL"));
    		lista.add(bean);
		}
			
		return lista;
	
	}
	
		
	@Override
	public List<TxxxxBean> consultarBandeja(TxxxxBean txxxxBean) throws Exception{
		
		List<TxxxxBean> lista = new ArrayList<TxxxxBean>(); 
		List<Map<String, Object>>listaRetorno = jdbcTemplate.queryForList(this.findBandejaFacturadorTodo);
		
		String fecCarg = null;
		String fecGene = null;
		String fecEnvi = null;
		TxxxxBean bean = null;
		for (Map<String,Object> row : listaRetorno) {
			fecCarg = row.get("FEC_CARG")!=null?(String)row.get("FEC_CARG"):"-";
			fecGene = row.get("FEC_GENE")!=null?(String)row.get("FEC_GENE"):"-";
			fecEnvi = row.get("FEC_ENVI")!=null?(String)row.get("FEC_ENVI"):"-";
			bean = new TxxxxBean();
			bean.setNum_ruc((String)row.get("NUM_RUC"));
    		bean.setTip_docu((String)row.get("TIP_DOCU"));
    		bean.setNum_docu((String)row.get("NUM_DOCU"));
    		bean.setFec_carg(fecCarg);
    		bean.setFec_gene(fecGene);
    		bean.setFec_envi(fecEnvi);
    		bean.setDes_obse((String)row.get("DES_OBSE"));
    		bean.setNom_arch((String)row.get("NOM_ARCH"));
    		bean.setInd_situ((String)row.get("IND_SITU"));
    		bean.setTip_arch((String)row.get("TIP_ARCH"));
    		bean.setNum_ruc((String)row.get("NUM_RUC"));
    		bean.setFirm_digital((String)row.get("FIRM_DIGITAL"));
    		lista.add(bean);
		}
			
		return lista;
	
	}
	
	@Override
	public List<TxxxxBean> consultarBandejaPorSituacion(TxxxxBean txxxxBean) throws Exception{
					
		String indSituacion = txxxxBean.getInd_situ()!=null?txxxxBean.getInd_situ():"XX";
		List<TxxxxBean> lista = new ArrayList<TxxxxBean>(); 
		List<Map<String, Object>>listaRetorno = jdbcTemplate.queryForList(this.findBandejaFacturadorSitu,indSituacion);
		
		TxxxxBean bean = null;
		for (Map<String,Object> row : listaRetorno) {
			bean = new TxxxxBean();
    		bean.setNum_ruc((String)row.get("NUM_RUC"));
    		bean.setTip_docu((String)row.get("TIP_DOCU"));
    		bean.setNum_docu((String)row.get("NUM_DOCU"));
    		bean.setFec_carg((String)row.get("FEC_CARG"));
    		bean.setFec_gene((String)row.get("FEC_GENE"));
    		bean.setFec_envi((String)row.get("FEC_ENVI"));
    		bean.setDes_obse((String)row.get("DES_OBSE"));
    		bean.setNom_arch((String)row.get("NOM_ARCH"));
    		bean.setInd_situ((String)row.get("IND_SITU"));
    		bean.setTip_arch((String)row.get("TIP_ARCH"));
    		bean.setFirm_digital((String)row.get("FIRM_DIGITAL"));
    		lista.add(bean);
		}
			
		return lista;
	
	}

	@Override
	public void insertarBandeja(TxxxxBean txxxxBean) throws Exception{
		
		jdbcTemplate.update(this.inseBandejaFacturadorTodo,new Object[] { txxxxBean.getNum_ruc(),  txxxxBean.getTip_docu(), txxxxBean.getNum_docu(), 
																		  txxxxBean.getFec_carg(), txxxxBean.getFec_gene(), txxxxBean.getFec_gene(), 
																		  txxxxBean.getDes_obse(), txxxxBean.getNom_arch(), txxxxBean.getInd_situ(),
																		  txxxxBean.getTip_arch()},
														   new int[]	{ Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,
																		  Types.DATE,Types.DATE,Types.DATE,
																		  Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,
																		  Types.VARCHAR});
			
	}

	@Override
	public Integer contarBandejaPorNomArch(TxxxxBean txxxxBean) throws Exception{
		
		Integer retorno = jdbcTemplate.queryForObject(this.findBandejaFacturadorArch,Integer.class,txxxxBean.getNom_arch());
		
		return retorno;
		
	}
	
	@Override
	public void actualizarBandeja(TxxxxBean txxxxBean) throws Exception{
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fechaActual = format.format(new Date());
		
		if("FECHA_GENERACION".equals(txxxxBean.getFec_gene()))
			this.jdbcTemplate.update(this.updaBandejaFacturadorGene,txxxxBean.getInd_situ(),txxxxBean.getDes_obse(), fechaActual , txxxxBean.getNom_arch());
		
		if("FECHA_ENVIO".equals(txxxxBean.getFec_envi()))
			this.jdbcTemplate.update(this.updaBandejaFacturadorEnvi,txxxxBean.getInd_situ(),txxxxBean.getDes_obse(), fechaActual , txxxxBean.getNom_arch());
		
		if(!"FECHA_GENERACION".equals(txxxxBean.getFec_gene())&&!"FECHA_ENVIO".equals(txxxxBean.getFec_envi()))
			this.jdbcTemplate.update(this.updaBandejaFacturadorArch,txxxxBean.getInd_situ(),txxxxBean.getDes_obse(), txxxxBean.getNom_arch());

    }	
}
