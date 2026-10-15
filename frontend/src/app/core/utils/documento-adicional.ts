import { Observable, map, of, switchMap, throwError } from 'rxjs';
import { CatalogoAcademicoService } from '../services/catalogo-academico-service';
import { SolicitudAcademicaService } from '../services/solicitud-academica-service';
import { AnexoAcademicoDTORespuesta } from '../models/SolicitudAcademica/DTOResponse/AnexoAcademicoDTORespuesta';
import { ACCIONES_CON_DOCUMENTO_ADICIONAL, AccionAcademica, DOCUMENTO_ADICIONAL_RESPUESTA } from '../constantes/procesos-academicos';

function normalizado(texto: string | null | undefined): string {
  return (texto ?? '').trim().normalize('NFC').toLowerCase();
}

export function esDocumentoAdicional(nombreTipo: string | null | undefined): boolean {
  return normalizado(nombreTipo) === normalizado(DOCUMENTO_ADICIONAL_RESPUESTA);
}

export function tieneDocumentoAdicional(anexos: AnexoAcademicoDTORespuesta[] | null | undefined): boolean {
  return (anexos ?? []).some(anexo => esDocumentoAdicional(anexo.tipoAnexo));
}

export function admiteDocumentoAdicional(accion: AccionAcademica | null): boolean {
  return accion !== null && ACCIONES_CON_DOCUMENTO_ADICIONAL.includes(accion);
}

export const TITULO_FALLA_DOCUMENTO_ADICIONAL = 'No se pudo subir el documento adicional';

export function mensajeDeFalla(err: unknown): string {
  const cuerpo = err && typeof err === 'object' && 'error' in err ? (err as { error: unknown }).error : null;
  if (cuerpo && typeof cuerpo === 'object' && 'mensaje' in cuerpo && typeof (cuerpo as { mensaje: unknown }).mensaje === 'string') {
    return (cuerpo as { mensaje: string }).mensaje;
  }
  if (err && typeof err === 'object' && 'message' in err && typeof (err as { message: unknown }).message === 'string') {
    return (err as { message: string }).message;
  }
  return 'Error desconocido';
}

export function subirDocumentoAdicional(
  catalogoAcademicoService: CatalogoAcademicoService,
  solicitudAcademicaService: SolicitudAcademicaService,
  uuidTipoSolicitud: string,
  uuidSolicitud: string,
  archivo: File | null
): Observable<void> {
  if (!archivo) return of(undefined);
  return catalogoAcademicoService.getTiposAnexoPorTipo(uuidTipoSolicitud).pipe(
    switchMap(tipos => {
      const tipo = (tipos ?? []).find(t => esDocumentoAdicional(t.nombre));
      if (!tipo) return throwError(() => ({ message: `El catálogo no tiene el tipo de anexo ${DOCUMENTO_ADICIONAL_RESPUESTA}` }));
      return solicitudAcademicaService.adjuntarAnexo(uuidSolicitud, { archivo, tipoAnexo: tipo.uuidTipoAnexoAcademico });
    }),
    map(() => undefined)
  );
}
