import { FuncionarioTipoSolicitudDTORespuesta } from "./FuncionarioTipoSolicitudDTORespuesta";
import { TipoAnexoDTORespuesta } from "./TipoAnexoDTORespuesta";

export class TipoSolicitudDTORespuesta {
    uuidTipoSolicitud!: string;
    nombre!: string;
    descripcion!: string;
    seccion!: string;
    perfilSolicitante!: string;
    anexos!: TipoAnexoDTORespuesta[];
    objFuncionarioEncargado!: FuncionarioTipoSolicitudDTORespuesta;
}