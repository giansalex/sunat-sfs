package sfs.model.dao.spring;


import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import sfs.model.dao.TxxxyDAO;
import sfs.model.domain.TxxxyBean;

public class TxxxyDAOImpl implements TxxxyDAO {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = -7620828220507189880L;
	
	private final String findParametrosTodo = "SELECT ID_PARA,COD_PARA,NOM_PARA, TIP_PARA, VAL_PARA,IND_ESTA_PARA FROM TXXXX_PARAM WHERE ID_PARA = ? AND COD_PARA = ?";
	private final String findParametrosById = "SELECT ID_PARA,COD_PARA,NOM_PARA, TIP_PARA, VAL_PARA,IND_ESTA_PARA FROM TXXXX_PARAM WHERE ID_PARA = ? ";
	private final String insertarParametros = "INSERT INTO TXXXX_PARAM (ID_PARA,COD_PARA,NOM_PARA, TIP_PARA, VAL_PARA,IND_ESTA_PARA) VALUES(?,?,?,?,?,?)";
	private final String actualizarParametro = "UPDATE TXXXX_PARAM SET VAL_PARA = ? WHERE ID_PARA = ? AND COD_PARA = ?";

	private JdbcTemplate jdbcTemplate;	
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public List<TxxxyBean> consultarParametroById(TxxxyBean txxxyBean){
		
		String idPara = txxxyBean.getId_para();
		
		List<TxxxyBean> lista = new ArrayList<TxxxyBean>(); 
		List<Map<String, Object>>listaRetorno = jdbcTemplate.queryForList(this.findParametrosById, new Object[] {idPara });
		
		TxxxyBean bean = null;
		for (Map<String,Object> row : listaRetorno) {
			bean = new TxxxyBean();
    		bean.setId_para((String)row.get("ID_PARA"));
    		bean.setCod_para((String)row.get("COD_PARA"));
    		bean.setNom_para((String)row.get("NOM_PARA"));
    		bean.setTip_para((String)row.get("TIP_PARA"));
    		bean.setVal_para((String)row.get("VAL_PARA"));
    		bean.setInd_esta_para((String)row.get("IND_ESTA_PARA"));
    		lista.add(bean);
		}
			
		return lista;
	
	}
	
	@Override
	public List<TxxxyBean> consultarParametro(TxxxyBean txxxyBean){
		
		String idPara = txxxyBean.getId_para();
		String codPara = txxxyBean.getCod_para(); 
		
		List<TxxxyBean> lista = new ArrayList<TxxxyBean>(); 
		List<Map<String, Object>>listaRetorno = jdbcTemplate.queryForList(this.findParametrosTodo, new Object[] {idPara, codPara });
		
		TxxxyBean bean = null;
		for (Map<String,Object> row : listaRetorno) {
			bean = new TxxxyBean();
    		bean.setId_para((String)row.get("ID_PARA"));
    		bean.setCod_para((String)row.get("COD_PARA"));
    		bean.setNom_para((String)row.get("NOM_PARA"));
    		bean.setTip_para((String)row.get("TIP_PARA"));
    		bean.setVal_para((String)row.get("VAL_PARA"));
    		bean.setInd_esta_para((String)row.get("IND_ESTA_PARA"));
    		lista.add(bean);
		}
		
		return lista;
	
	}
	
	@Override
	public void insertarParametro(TxxxyBean txxxyBean){
		
		jdbcTemplate.update(this.insertarParametros,new Object[] { txxxyBean.getId_para(), txxxyBean.getCod_para(), txxxyBean.getNom_para(), 
				                                                   txxxyBean.getTip_para(), txxxyBean.getVal_para(), txxxyBean.getInd_esta_para()},
														   new int[]	{ Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,
																		  Types.VARCHAR,Types.VARCHAR,Types.VARCHAR});
	}
	
	@Override
	public void actualizarParametro(TxxxyBean txxxyBean){
		
		jdbcTemplate.update(this.actualizarParametro, txxxyBean.getVal_para(), txxxyBean.getId_para(), txxxyBean.getCod_para());
		
	}
	
	
}
