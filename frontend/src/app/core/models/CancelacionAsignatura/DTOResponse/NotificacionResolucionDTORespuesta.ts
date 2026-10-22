export interface NotificacionResolucionDTORespuesta {
  estado: 'PROGRAMADA' | 'EN_PROCESO' | 'ENVIADA' | 'FALLIDA';
  fechaProgramada: string;
  firmada: boolean;
  numeroResolucion: string;
  anioResolucion: number;
  sufijoResolucion: string;
  decision: string;
  mensajeInicial: string | null;
  textoComunicacion: string | null;
  textoDarca: string | null;
  textoDarcaFirma: string | null;
  fechaEnvioEstudiante: string | null;
  fechaEnvioDarca: string | null;
  intentos: number;
  ultimoError: string | null;
  fechaEnvio: string | null;
  fechaLiberacion: string | null;
  editable: boolean;
}
