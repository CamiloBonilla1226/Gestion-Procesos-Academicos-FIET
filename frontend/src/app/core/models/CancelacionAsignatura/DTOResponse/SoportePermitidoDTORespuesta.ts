export interface SoportePermitidoDTORespuesta {
  uuidTipoAnexoAcademico: string | null;
  nombre: string;
  formatosPermitidos: string;
  tamanioMaximoBytes: number;
  obligatorio: boolean;
}
