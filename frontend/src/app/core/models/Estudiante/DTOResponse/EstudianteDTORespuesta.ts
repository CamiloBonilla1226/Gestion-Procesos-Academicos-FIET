import { AsignaturaMatriculadaDTORespuesta } from './AsignaturaMatriculadaDTORespuesta';

export interface EstudianteDTORespuesta {
  uuidUsuario: string;
  nombres: string;
  apellidos: string;
  estado: boolean;
  tipoDocumento: string;
  numeroDocumento: string;
  telefono: string;
  correoElectronico: string;
  username: string;
  codigoEstudiantil: string;
  programaAcademico: string;
  semestre: string;
  facultad: string;
  asignaturasMatriculadas: AsignaturaMatriculadaDTORespuesta[];
}
