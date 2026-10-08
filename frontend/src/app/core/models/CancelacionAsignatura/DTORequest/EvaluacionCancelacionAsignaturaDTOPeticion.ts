export interface EvaluacionCancelacionAsignaturaDTOPeticion {
  asignaturaSolicitudUuid: string;
  numeroFaltas: number;
  nota: number;
  situacionMatriculaUuid: string;
  cumpleCondiciones: boolean;
  observacionEvaluacion: string | null;
}
