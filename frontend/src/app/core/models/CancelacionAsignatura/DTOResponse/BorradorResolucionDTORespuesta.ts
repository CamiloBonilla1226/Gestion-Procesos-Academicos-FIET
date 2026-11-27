export interface FilaResolucionDTO {
  asignatura: string;
  faltas: string;
  nota: string;
  estadoAlSolicitar: string;
  estadoCancelacion: string;
  estadoNuevaMatricula: string;
}

export interface BorradorResolucionDTORespuesta {
  asignaturaSolicitudUuid: string;
  asignatura: string;
  aceptada: boolean;
  prefijo: string;
  numero: string;
  fecha: string;
  asunto: string;
  preambulo: string;
  considerandos: string;
  articuloPrimero: string;
  filas: FilaResolucionDTO[];
  articulosSiguientes: string;
  firmante: string;
  cargoFirmante: string;
  diligencia: string;
  proyecto: string;
  reviso: string;
}
