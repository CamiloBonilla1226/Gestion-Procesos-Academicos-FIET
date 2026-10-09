import { Type } from '@angular/core';
import { ProcesoAcademico, RolEtiqueta } from '../../../../core/constantes/procesos-academicos';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';

export interface EntradasComponenteProceso {
  uuidSolicitud: string;
  solicitud: SolicitudAcademicaDetalleDTORespuesta;
  rol: RolEtiqueta;
  recargar: () => void;
}

export type CargadorComponenteProceso = () => Promise<Type<unknown>>;

export const COMPONENTES_PROCESO: Partial<Record<ProcesoAcademico, CargadorComponenteProceso>> = {};
