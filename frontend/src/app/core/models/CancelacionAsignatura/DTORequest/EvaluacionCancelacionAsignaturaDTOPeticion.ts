export interface EvaluacionCancelacionAsignaturaDTOPeticion {
  asignaturaSolicitudUuid: string;
  numeroFaltas: string | null;
  nota: string | null;
  situacionMatriculaUuid: string | null;
  situacionCancelarUuid: string | null;
  cumpleCondiciones: boolean;
  observacionEvaluacion: string | null;
}
