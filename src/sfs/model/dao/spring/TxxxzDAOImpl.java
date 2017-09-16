package sfs.model.dao.spring;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import sfs.model.dao.TxxxzDAO;
import sfs.model.domain.TxxxzBean;
import sfs.util.FacturadorUtil;

public class TxxxzDAOImpl implements TxxxzDAO {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = -7620828220507189880L;
	
	private final String findErrorById = "SELECT COD_CATAERRO, NOM_CATAERRO,IND_ESTADO FROM TXXX_CATAERRO WHERE COD_CATAERRO = ?";
	
	private JdbcTemplate jdbcTemplate;	
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public TxxxzBean consultarErrorById(Integer idError){
		TxxxzBean txxxzBean = null;			
		
		if(idError.intValue() > 0){
			String codError = FacturadorUtil.completarCeros(idError.toString(), "I", 4);
			List<Map<String, Object>>listaRetorno = jdbcTemplate.queryForList(this.findErrorById,codError);
			if(listaRetorno.size() > 0){
				Map<String, Object> retorno = listaRetorno.get(0);
				txxxzBean = new TxxxzBean();
				txxxzBean.setCod_error((String)retorno.get("COD_CATAERRO"));
				txxxzBean.setNom_error((String)retorno.get("NOM_CATAERRO"));
				txxxzBean.setInd_estado((String)retorno.get("IND_ESTADO"));
			}
		}

		return txxxzBean;
	
	}
	
}
