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

export const COMPONENTES_PROCESO: Partial<Record<ProcesoAcademico, CargadorComponenteProceso>> = {
  'cancelaciones-matricula': () => import('../cancelacion-matricula-proceso-component/cancelacion-matricula-proceso-component').then(m => m.CancelacionMatriculaProcesoComponent),
  'cancelaciones-asignatura': () => import('../cancelacion-asignatura-proceso-component/cancelacion-asignatura-proceso-component').then(m => m.CancelacionAsignaturaProcesoComponent)
};
