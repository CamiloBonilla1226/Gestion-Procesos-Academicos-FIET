import { SolicitudDTORespuesta } from "../../Solicitudes/DTOResponse/SolicitudDTORespuesta";

export class RespuestaDTORespuesta {
    uuidRespuesta!: string;
    tipoRespuesta!: string;
    consecutivoFiet!: string;
    respuestaConsejo!: string;
    indicaciones!: string;
    solicitud!: SolicitudDTORespuesta;
    urlRespuesta!: string;
}