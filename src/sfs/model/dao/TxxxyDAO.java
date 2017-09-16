package sfs.model.dao;

import java.util.List;

import sfs.model.domain.TxxxyBean;

public interface TxxxyDAO {
	
	public List<TxxxyBean> consultarParametroById(TxxxyBean txxxyBean);
	public List<TxxxyBean> consultarParametro(TxxxyBean txxxyBean);
	public void insertarParametro(TxxxyBean txxxyBean);
	public void actualizarParametro(TxxxyBean txxxyBean);

}
