export interface AnexoAcademicoDTORespuesta {
  uuidAnexoAcademico: string;
  nombreArchivo: string;
  uuidTipoAnexoAcademico: string | null;
  tipoAnexo: string | null;
  tipoArchivo: string;
  tamanioBytes: number;
  fechaSubida: string;
}
