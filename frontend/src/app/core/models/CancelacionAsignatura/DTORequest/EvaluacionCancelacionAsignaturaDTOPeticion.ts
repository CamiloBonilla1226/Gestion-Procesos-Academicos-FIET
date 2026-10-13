export interface EvaluacionCancelacionAsignaturaDTOPeticion {
  asignaturaSolicitudUuid: string;
  numeroFaltas: string;
  nota: string;
  situacionMatriculaUuid: string;
  situacionCancelarUuid: string | null;
  cumpleCondiciones: boolean;
  observacionEvaluacion: string | null;
}
