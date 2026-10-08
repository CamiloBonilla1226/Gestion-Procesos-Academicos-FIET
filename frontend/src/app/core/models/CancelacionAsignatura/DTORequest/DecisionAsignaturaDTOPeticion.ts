export interface DecisionAsignaturaDTOPeticion {
  asignaturaSolicitudUuid: string;
  aprobada: boolean;
  situacionCancelarUuid: string | null;
  observacionDecision: string | null;
}
