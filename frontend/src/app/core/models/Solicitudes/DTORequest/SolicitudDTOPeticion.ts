import { AnexoDTOPeticion } from "./AnexoDTOPeticion";

export class SolicitudDTOPeticion {
    consecutivo!: string ;
    nombre!: string ;
    descripcion!: string ;
    uuidTipoSolicitud!: string ;
    anexos!: AnexoDTOPeticion[] ;
    uuidOrdenDelDia!: string ;
}