export interface EvaluacionAsignaturaDTOPeticion {
  asignaturaSolicitudUuid: string;
  numeroFaltas: string;
  nota: string;
  situacionMatriculaUuid: string;
  situacionCancelarUuid: string | null;
}
