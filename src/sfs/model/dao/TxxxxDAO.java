package sfs.model.dao;

import java.util.List;

import sfs.model.domain.TxxxxBean;

public interface TxxxxDAO {
	
	public List<TxxxxBean> consultarBandejaPorId(TxxxxBean txxxxBean) throws Exception;
	public List<TxxxxBean> consultarBandeja(TxxxxBean txxxxBean) throws Exception;
	public List<TxxxxBean> consultarBandejaPorSituacion(TxxxxBean txxxxBean) throws Exception;
	public void insertarBandeja(TxxxxBean txxxxBean) throws Exception;
	public Integer contarBandejaPorNomArch(TxxxxBean txxxxBean) throws Exception;
	public void actualizarBandeja(TxxxxBean txxxxBean) throws Exception;
	public void eliminarBandeja(TxxxxBean txxxxBean) throws Exception;

}
