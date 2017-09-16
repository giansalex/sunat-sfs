<!DOCTYPE html>
<html lang="en">
	<head>
	    <meta charset="utf-8">
    	<meta http-equiv="X-UA-Compatible" content="IE=edge">
    	<meta name="viewport" content="width=device-width, initial-scale=1">
	</head>
	<script type="text/javascript">
	 var lista = ${listaBandejaFacturador};
	 var tipoFuncionalidad = ${tipoFuncionalidad};
	 var mensajeError = ${mensajeError};
	 var tiempoMinutos = ${tiempoTemporizador};
	</script>
	<!-- Cargando estilos -->
	<link rel="stylesheet" type="text/css" href="/a/js/libs/bootstrap/3.3.2/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="/a/js/libs/bootstrap/3.3.2/css/bootstrap-theme.min.css">
	<link rel="stylesheet" type="text/css" href="/a/js/libs/bootstrap/3.3.2/plugins/datatables-1.10.7/media/css/jquery.dataTables.min.css">
 	<link rel="stylesheet" type="text/css" href="/a/js/libs/bootstrap/3.3.2/plugins/datatables-1.10.7/extensions/Responsive/css/dataTables.responsive.css">
	<!-- Cargando Librerias -->
	<script type="text/javascript" src="/a/js/libs/jquery/1.11.2/jquery.min.js"></script>
	<script type="text/javascript" src="/a/js/libs/bootstrap/3.3.2/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/a/js/libs/bootstrap/3.3.2/plugins/datatables-1.10.7/media/js/jquery.dataTables.min.js"></script>
 	<script type="text/javascript" src="/a/js/libs/bootstrap/3.3.2/plugins/datatables-1.10.7/extensions/Responsive/js/dataTables.responsive.js"></script>
	<!--[if lt IE 9]>
    <script type="text/javascript" src="/a/js/libs/bootstrap/3.3.2/plugins/html5shiv/3.7.2/html5shiv.min.js"></script>
	<script type="text/javascript" src="/a/js/libs/bootstrap/3.3.2/plugins/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    <script type="text/javascript" src="/a/js/swfacturador/consultaBandeja.js"></script>
    <style>   
    .titulos {
	  	  font-size: 12px;
		}
		
	.error{
	  
	  color:#ff0000;
	  
	}	
	
	.alineaIzquiera { 
		text-align: left; 
	}
	
	th {
  			text-align: center; 
		}
	
	.bordeSunat{	
		  border: 1px solid #337ab7;
		  }
		  
  	h4 {
  		color: #337ab7;
  		font-weight: Bold;
	}
	</style>   
 <body>	
 <div class="container-fluid">
    <div class="row">
        <div class="col-md-12">
            <div class="panel panel-body">
                <div class="panel-heading bordeSunat">
                   <div class="row">
                   		<div class="col-xs-2 col-md-2 text-left"><img src="/a/imagenes/logo_2015.png"></div>
                   		<div class="col-xs-8 col-md-8 text-left">&nbsp;</div>
                   		<div class="col-xs-2 col-md-2 text-right">
                   			<button id="btnRefrescar" class="btn btn-sm btn-primary"><i class="glyphicon glyphicon-refresh"></i></button>
                  			<button id="btnVisorCdp" class="btn btn-sm btn-primary"><i class="glyphicon glyphicon-print"></i></button>
                  			<button id="btnLimpiar" class="btn btn-sm btn-primary"><i class="glyphicon glyphicon-erase"></i></button>
	                   		<div class="btn-group">
							  <button type="button" class="btn btn-sm btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="glyphicon glyphicon-wrench"></i></button>
							  <ul class="dropdown-menu pull-right">
							    <li><a href="#" onclick="javascript:configurar()">Parámetros de Configuración</a></li>
							    <li><a href="#" onclick="javascript:importarCertificado()">Importar Certificado</a></li>
							    <li role="separator" class="divider"></li>
							    <li><a href="#" onclick="javascript:configurarOtros()">Otros Par&aacute;metros</a></li>
							    <li role="separator" class="divider"></li>
							    <li><a href="#" onclick="javascript:actualizarVersion()">Actualizar Versi&oacute;n del Facturador</a></li>
							    <li><a href="#" onclick="javascript:acercaDe()">Acerca del Facturador SUNAT</a></li>
							  </ul>
							</div>
						</div>
                   	</div>
                   	<div class="row">
                   		<div class="col-xs-6 col-md-6 text-left"><h4>Sistema Facturador SUNAT - Software Gratuito</h4></div>
                   		<div class="col-xs-6 col-md-6 text-right"><h4>Contribuyente: ${numRuc} - ${razonSocial}</h4></div>
                   	</div>	
                </div>
                <div class="panel-body">
                    <div class="row">
                        <div class="col-xs-12 col-md-12 text-center">
                        	<div class="table-responsive">
		                        <table id="tDetail" class="display responsive" style="width:'100%'">
								    <thead>
										<tr>
											<th>Nro</th>
											<th>Nro. RUC</th>
											<th>Tipo Doc.</th>
											<th>N&uacute;mero Doc.</th>
										    <th class="never">Fecha Carga</th>
										    <th>Fecha Generaci&oacute;n</th>
										    <th>Fecha Env&iacute;o</th>
										    <th>Situaci&oacute;n</th>
										    <th>Observaciones</th>
										</tr>
									</thead>
								</table>
							</div>
                        </div>
                    </div>
               	</div>
               	<div id="piePagina" class="panel-footer">
	               	<div class="row">
						<div class="col-xs-6 col-md-6 text-right"><button type="button" class="btn btn-primary" id="btnGenerar">Generar Comprobante SUNAT</button></div>
						<div class="col-xs-6 col-md-6 text-left"><button type="button" class="btn btn-primary" id="btnEnviar">Env&iacute;ar Comprobante SUNAT</button></div>
					</div>
				</div>
				<div id="piePaginaVersion" class="panel-footer">
	               	<div class="row">
						<div class="col-xs-12 col-md-12 text-center">versi&oacute;n ${version}</div>
					</div>
				</div>
				<div id="horaEjecucionAuto" class="panel-footer">
	               	<div class="row">
						<div class="col-xs-12 col-md-12 text-left"><label for="lbMensajeHoraAuto"></label></div>
					</div>
				</div>
            </div>
        </div>
    </div>
</div>

<!-- Modal de Datos de Configuración - Inicio -->
<div id="ventanaConfiguracion" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="h4ModalConfiguracion">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Cerrar"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="h4ModalLabel01">Pantalla de Configuraci&oacute;n</h4>
			</div>
			<div class="modal-body">
			<fieldset>
    			<legend class="titulos">Datos del Emisor</legend>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">N&uacute;mero de RUC: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="text" class="form-control" id="txtNumeroRuc" name="txtNumeroRuc" maxlength="11" placeholder="Nro. RUC"></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Raz&oacute;n Social: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="text" class="form-control" id="txtRazonSocial" name="txtRazonSocial" maxlength="160" placeholder="Raz&oacute;n Social"></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Usuario SOL: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="text" class="form-control" id="txtUsuarioSol" name="txtUsuarioSol" maxlength="8" placeholder="Usuario SOL"></div>
				</div>
				<br/>		
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Clave SOL: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="password" class="form-control" id="txtClaveSol" name="txtClaveSol" maxlength="30" placeholder="Clave SOL"></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Usa Temporizador: </div>
					<div class="col-xs-9 col-md-9 text-left"><select id="cmbFuncionamiento"><option value="">[Seleccione]</option><option value="01">Si</option><option value="02">No</option></select></div>
				</div>
				<br/>
				<div id="minutos">
					<div class="row">
						<div class="col-xs-3 col-md-3 text-right">En Generar Cdp </div>
						<div class="col-xs-2 col-md-2 text-left"><select id="cmbTiempoGenera"><option value="">[Seleccione]</option><option value="5">5 Segundos</option><option value="10">10 Segundos</option><option value="20">20 Segundos</option><option value="30">30 Segundos</option><option value="40">40 Segundos</option><option value="50">50 Segundos</option><option value="59">59 Segundos</option></select></div>
					</div>
					<br/>
					<div class="row">
						<div class="col-xs-3 col-md-3 text-right">En Enviar Cdp </div>
						<div class="col-xs-2 col-md-2 text-left"><select id="cmbTiempoEnviar"><option value="">[No Aplica]</option><option value="1">1 Minuto</option><option value="2">2 Minutos</option><option value="5">5 Minutos</option><option value="10">10 Minutos</option><option value="20">20 Minutos</option><option value="30">30 Minutos</option><option value="40">40 Minutos</option><option value="50">50 Minutos</option></select></div>
					</div>
					<br/>
				</div>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Ruta de Trabajo: </div>
					<div class="col-xs-9 col-md-9 text-left"><input id="txtRutaSolucion" type="text" class="form-control" name="txtRutaSolucion" maxlength="30" ></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Certificado: </div>
					<div class="col-xs-9 col-md-9 text-left"><input id="txtCertificado" type="text" class="form-control" name="txtCertificado" maxlength="30" readonly></div>
				</div>
			</fieldset>
			</div>
			<br/>
			<fieldset>
			<div class="row">
				<div class="col-xs-12 col-md-12 text-center"><label for="lbError" style="color:Red"></label></div>
			</div>
			</fieldset>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" id="btnAceptar">Aceptar</button>
				<button type="button" class="btn btn-primary" id="btnCancelar">Cancelar</button>
		    </div>
		</div>
	</div>
</div>
<!-- Modal de Datos de Configuración - Fin -->

<!-- Modal de Crear Firma Digital - Inicio -->
<div id="ventanaImportarCertificado" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="h4ModalConfiguracion">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Cerrar"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="h4ModalLabel01">Importar Certificado</h4>
			</div>
			<div class="modal-body">
			<div class="row">
			<div class="col-xs-10 col-md-10 text-left titulos">*Los certificados deben ser copiados por usted, en la ruta: data0/facturador/CERT</div>
			<div class="col-xs-2 col-md-2 text-left">&nbsp;</div>
			</div>
			<br/>
			<fieldset>
    			<legend class="titulos">Datos del Certificado</legend>
				<div class="row">
					<div class="col-xs-6 col-md-6 text-right titulos">Seleccione Certificado (*): </div>
					<div class="col-xs-6 col-md-6 text-left">
						<select id="cmbRutaCertificado">
							<option value="">Seleccione Certificado</option>
						</select>
					</div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-6 col-md-6 text-right titulos">Contraseña Certificado: </div>
					<div class="col-xs-6 col-md-6 text-left"><input id="txtPassPrivateKey" type="password" class="form-control" name="txtPassPrivateKey" maxlength="30"></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-6 col-md-6 text-right"><button type="button" class="btn btn-primary" id="btnCargarListaCertificado">Cargar Lista</button></div>
					<div class="col-xs-6 col-md-6 text-left">&nbsp;</div>
				</div>
			</fieldset>
			<br/>
			<fieldset>
			<div class="row">
				<div class="col-xs-12 col-md-12 text-center"><label for="lbError" style="color:Red"></label></div>
			</div>
			</fieldset>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" id="btnImportarCertificado">Importar</button>
				<button type="button" class="btn btn-primary" id="btnCancelarCertificado">Cancelar</button>
		    </div>
		</div>
	</div>
</div>
<!-- Modal de Crear Firma Digital - Fin -->
<!-- Modal de Otros Datos de Configuración - Inicio -->
<div id="ventanaOtrosConfiguracion" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="h4ModalOtrosConfiguracion">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Cerrar"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="h4ModalLabel01">Pantalla de Otros Datos de Configuraci&oacute;n</h4>
			</div>
			<div class="modal-body">
			<fieldset>
    			<legend class="titulos">Otros Datos de Configuraci&oacute;n</legend>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Nombre Comercial: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="text" class="form-control" id="txtNombreComercial" name="txtNombreComercial" maxlength="120" placeholder="Nombre Comercial"></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Ubigeo Domicilio Fiscal: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="text" class="form-control" id="txtUbigeo" name="txtUbigeo" maxlength="6" placeholder="C&oacute;digo Ubigeo"></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Direcci&oacute;n Domicilio Fiscal: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="text" class="form-control" id="txtDireccion" name="txtDireccion" maxlength="120" placeholder="Direcci&oacute;n"></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Urbanizaci&oacute;n: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="text" class="form-control" id="txtUrbanizacion" name="txtUrbanizacion" maxlength="120" placeholder="Urbanizaci&oacute;n"></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Departamento: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="text" class="form-control" id="txtDepartamento" name="txtDepartamento" maxlength="120" placeholder="Departamento"></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Provincia: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="text" class="form-control" id="txtProvincia" name="txtProvincia" maxlength="120" placeholder="Provincia"></div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Distrito: </div>
					<div class="col-xs-9 col-md-9 text-left"><input type="text" class="form-control" id="txtDistrito" name="txtDistrito" maxlength="120" placeholder="Distrito"></div>
				</div>
				<br/>
			</fieldset>
			</div>
			<br/>
			<fieldset>
			<div class="row">
				<div class="col-xs-12 col-md-12 text-center"><label for="lbError" style="color:Red"></label></div>
			</div>
			</fieldset>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" id="btnAceptarOtro">Aceptar</button>
				<button type="button" class="btn btn-primary" id="btnCancelarOtro">Cancelar</button>
		    </div>
		</div>
	</div>
</div>
<!-- Modal de Otros Datos de Configuración - Fin -->
<!-- Modal de Acerca de - Inicio -->
<div id="ventanaAcercaDe" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="h4ModalAcercaDe">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Cerrar"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="h4ModalLabel01">Acerca del Facturador SUNAT</h4>
			</div>
			<div class="modal-body">
				<div class="row">
					<div class="col-xs-12 col-md-12 text-center"><IMG src="/a/js/swfacturador/logoSUNAT2.jpg" /></div>
				</div>
				<br/>
				<fieldset>
    			<legend class="text-center">Programa Facturador de SUNAT</legend>
					<div class="row">
						<div class="col-xs-12 col-md-12 text-center">Versi&oacute;n ${version}</div>
					</div>
					<div class="row">
						<div class="col-xs-12 col-md-12 text-center">Software Desarrollado por la SUNAT con</div>
					</div>
					<div class="row">
						<div class="col-xs-12 col-md-12 text-center">licencia de uso general</div>
					</div>
				</fieldset>	
				<br/>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" id="btnCerrarAcerca">Cerrar</button>
		    </div>
		</div>
	</div>
</div>
<!-- Modal de Acerca de - Fin -->
<!-- Modal de Excepciones - Inicio -->
<div id="ventanaExcepciones" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="h4ModalException">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Cerrar"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="h4ModalLabel01">Excepci&oacute;n en el Facturador SUNAT</h4>
			</div>
			<div class="modal-body">
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right"><IMG src="/a/js/swfacturador/sunatpc.jpg" /></div>
					<div class="col-xs-9 col-md-9 text-left error">Error en el Sistema Facturador SUNAT.</div>
				</div>
				<br/>
				<div class="row">
					<div class="col-xs-3 col-md-3 text-right">Detalle:</div>
					<div class="col-xs-9 col-md-9 text-left error" id="mensajeError"></div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" id="btnCerrarExcepciones">Cerrar</button>
		    </div>
		</div>
	</div>
</div>
<!-- Modal de Acerca de - Fin -->
<!-- Modal Visor de XML - Inicio -->
<div id="ventanaVisorXml" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="h4ModalException">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Cerrar"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="h4ModalLabel01">Visor Comprobantes - Facturador SUNAT</h4>
			</div>
			<div class="modal-body">
				<div class="row">
					<div class="col-xs-12 col-md-12 text-left"><label for="lbMensaje"></label></div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" id="btnCerrarVisor">Cerrar</button>
		    </div>
		</div>
	</div>
</div>
<!-- Modal de Acerca de - Fin -->
<!-- Modal Limpiar Bandeja - Inicio -->
<div id="ventanaLimpiar" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="h4ModalException">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Cerrar"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="h4ModalLabel01">Limpiar Bandeja - Mensaje de Confirmaci&oacute;n</h4>
			</div>
			<div class="modal-body">
				<div class="row">
					<div class="col-xs-12 col-md-12 text-left"><label for="lbMensajeConsulta">¿Esta seguro que desea borrar todos los comprobantes en la bandeja del facturador?</label></div>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12 col-md-12 text-center"><label for="lbError" style="color:Red"></label></div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" id="btnConformeLimpiar">Conforme</button>
				<button type="button" class="btn btn-primary" id="btnNoConformeLimpiar">No Conforme</button>
		    </div>
		</div>
	</div>
</div>
<!-- Modal Limpiar Bandeja - Fin -->

<!-- Modal Actualizar Version - Inicio -->
<div id="ventanaActualizador" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="h4ModalException">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Cerrar"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="h4ModalLabel01">Actualizar Versi&oacute;n S.F.S. - Mensaje de Confirmaci&oacute;n</h4>
			</div>
			<div class="modal-body">
				<div class="row">
					<div class="col-xs-12 col-md-12 text-left"><label for="lbMensajeConsulta">Este procedimiento actualizar&aacute; su versi&oacute;n del S.F.S. ¿Esta seguro que desea continuar?</label></div>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12 col-md-12 text-center"><label for="lbError" style="color:Red"></label></div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" id="btnConformeActualizar">Conforme</button>
				<button type="button" class="btn btn-primary" id="btnNoConformeActualizar">No Conforme</button>
		    </div>
		</div>
	</div>
</div>
<!-- Modal Actualizar Version - Fin -->


<form id="generarXmlForm" name="generarXmlForm" method="POST">
	<input type="hidden" id="hddNumRuc" name="hddNumRuc" />
	<input type="hidden" id="hddTipDoc" name="hddTipDoc" />
	<input type="hidden" id="hddNumDoc" name="hddNumDoc" />
	<input type="hidden" id="hddNomArc" name="hddNomArc" />
	<input type="hidden" id="hddEstArc" name="hddEstArc" />
</form>
</body>
</html>
