export interface TipoAnexoAcademicoDTORespuesta {
  uuidTipoAnexoAcademico: string;
  uuidTipoSolicitudAcademica: string;
  nombre: string;
  formatosPermitidos: string;
  obligatorio: boolean;
  vigente?: boolean;
  tamanioMaximoBytes?: number | null;
}
