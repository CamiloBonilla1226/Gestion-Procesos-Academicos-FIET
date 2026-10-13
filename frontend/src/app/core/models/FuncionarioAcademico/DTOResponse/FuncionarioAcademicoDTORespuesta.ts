import { TipoSolicitudAtendidaDTORespuesta } from './TipoSolicitudAtendidaDTORespuesta';

export interface FuncionarioAcademicoDTORespuesta {
  uuidUsuario: string;
  nombres: string;
  apellidos: string;
  estado: boolean;
  tipoDocumento: string;
  numeroDocumento: string;
  telefono: string;
  correoElectronico: string;
  username: string;
  cargo: string;
  tiposSolicitud: TipoSolicitudAtendidaDTORespuesta[];
}
