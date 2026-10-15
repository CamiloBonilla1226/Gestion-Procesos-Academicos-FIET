export interface EvaluacionAsignaturaDTOPeticion {
  asignaturaSolicitudUuid: string;
  numeroFaltas: string | null;
  nota: string | null;
  situacionMatriculaUuid: string | null;
  situacionCancelarUuid: string | null;
}
