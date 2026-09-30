import { TipoSolicitudDTORespuesta } from "../../TipoSolicitud/DTOResponse/TipoSolicitudDTORespuesta";
import { AnexoDTORespuesta } from "./AnexoDTORespuesta";
import { FuncionarioDTORespuesta } from "./FuncionarioDTORespuesta";
import { InformacionSolicitanteDTORespuesta } from "./InformacionSolicitanteDTORespuesta";
import { OrdenDelDiaDTORespuesta } from "./OrdenDelDiaDTORespuesta";

export class SolicitudDTORespuesta {
    uuidSolicitud!: string ;
    consecutivo!: string ;
    nombre!: string ;
    descripcion!: string ;
    estado!: string ;
    tipoSolicitud!: TipoSolicitudDTORespuesta ;
    anexos!: AnexoDTORespuesta[] ;
    ordenDelDia!: OrdenDelDiaDTORespuesta ;
    informacionSolicitante!: InformacionSolicitanteDTORespuesta ;
    objFuncionario!: FuncionarioDTORespuesta ;
}