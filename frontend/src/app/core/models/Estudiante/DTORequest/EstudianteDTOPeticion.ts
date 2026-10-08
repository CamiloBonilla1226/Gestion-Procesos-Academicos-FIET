import { AsignaturaMatriculadaDTOPeticion } from './AsignaturaMatriculadaDTOPeticion';

export interface EstudianteDTOPeticion {
  nombres: string;
  apellidos: string;
  tipoDocumento: string;
  numeroDocumento: string;
  telefono: string;
  correoElectronico: string;
  username: string;
  password: string;
  codigoEstudiantil: string;
  programaAcademico: string;
  semestre: string;
  facultad: string;
  asignaturas: AsignaturaMatriculadaDTOPeticion[];
}
