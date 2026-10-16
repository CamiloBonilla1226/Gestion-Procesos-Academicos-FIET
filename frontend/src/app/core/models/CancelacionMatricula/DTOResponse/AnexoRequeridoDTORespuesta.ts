export interface AnexoRequeridoDTORespuesta {
  uuidTipoAnexoAcademico: string;
  nombre: string;
  formatosPermitidos: string;
  obligatorio: boolean;
  tamanioMaximoBytes?: number | null;
}
