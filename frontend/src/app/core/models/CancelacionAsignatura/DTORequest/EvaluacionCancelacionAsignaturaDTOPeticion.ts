export interface EvaluacionCancelacionAsignaturaDTOPeticion {
  asignaturaSolicitudUuid: string;
  numeroFaltas: string;
  nota: string;
  situacionMatriculaUuid: string;
  cumpleCondiciones: boolean;
  observacionEvaluacion: string | null;
}
