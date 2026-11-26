export interface ValorProvisionalFormatoDTOPeticion {
  asignaturaSolicitudUuid: string;
  situacionMatriculaUuid?: string | null;
  situacionCancelarUuid?: string | null;
  numeroFaltas?: string | null;
  nota?: string | null;
  aprobada?: boolean | null;
}
