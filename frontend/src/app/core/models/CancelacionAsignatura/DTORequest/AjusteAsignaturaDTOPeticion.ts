export interface AjusteAsignaturaDTOPeticion {
  asignaturaSolicitudUuid: string;
  situacionMatriculaUuid: string | null;
  situacionCancelarUuid: string | null;
  situacionNuevaMatriculaUuid: string | null;
  observacionDecision: string | null;
}
