import { Component, EventEmitter, Input, Output } from '@angular/core';
import { finalize, map, switchMap, throwError } from 'rxjs';
import { InputAnexoUploadComponent } from '../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { SimpleButtonComponent } from '../../buttons/simple-button-component/simple-button-component';
import { SolicitudAcademicaService } from '../../../core/services/solicitud-academica-service';
import { CatalogoAcademicoService } from '../../../core/services/catalogo-academico-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { ToastService } from '../../../core/services/toast-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { ValorProvisionalFormatoDTOPeticion } from '../../../core/models/SolicitudAcademica/DTORequest/ValorProvisionalFormatoDTOPeticion';
import {
  ETAPAS_ACADEMICAS,
  FORMATOS_RESOLUCION,
  ROLES_ETIQUETA,
  RolEtiqueta,
  TAMANIO_MAXIMO_PAZ_Y_SALVOS_BYTES
} from '../../../core/constantes/procesos-academicos';
import { comoTipoAnexo, errorDeArchivo } from '../../../core/utils/validaciones-academicas';
import { guardarArchivo } from '../../../core/utils/descargas';

export const FORMATO_FIRMADO_DECANO = 'Formato firmado por el Decano';
const AJUSTE_MAXIMO_MM = 15;
const ETAPAS_FORMATO_DECANO: readonly string[] = [ETAPAS_ACADEMICAS.APROBADA_POR_DECANO, ETAPAS_ACADEMICAS.RECHAZADA_POR_DECANO];

function normalizado(texto: string | null | undefined): string {
  return (texto ?? '').trim().normalize('NFC').toLowerCase();
}

@Component({
  selector: 'app-formato-oficial-component',
  imports: [InputAnexoUploadComponent, SimpleButtonComponent],
  templateUrl: './formato-oficial-component.html',
  styleUrl: '../../pages/content/estilos-academicos.css'
})
export class FormatoOficialComponent {
  @Input({ required: true }) uuidSolicitud!: string;
  @Input({ required: true }) solicitud!: SolicitudAcademicaDetalleDTORespuesta;
  @Input({ required: true }) rol!: RolEtiqueta;
  @Input() valoresProvisionales: () => ValorProvisionalFormatoDTOPeticion[] = () => [];
  @Input() firmaElDecano = false;
  @Input() decisionLista = true;
  @Output() formatoSubido = new EventEmitter<void>();

  readonly tipo = comoTipoAnexo('formato-decano', 'Formato firmado por el Decano (PDF)', FORMATOS_RESOLUCION, true);
  archivo: File | null = null;
  error = '';
  errorAjuste = '';
  verAjuste = false;
  ajusteX = 0;
  ajusteY = 0;
  descargando = false;
  guardandoAjuste = false;
  subiendo = false;
  abriendoEscaneo = false;
  abriendoFormatoDecano = false;

  constructor(
    private solicitudAcademicaService: SolicitudAcademicaService,
    private catalogoAcademicoService: CatalogoAcademicoService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  get esFuncionario(): boolean {
    return this.rol === ROLES_ETIQUETA.FUNCIONARIO;
  }

  get esDecano(): boolean {
    return this.rol === ROLES_ETIQUETA.DECANO;
  }

  get pideFormatoDecano(): boolean {
    return this.esFuncionario && !this.firmaElDecano && ETAPAS_FORMATO_DECANO.includes(this.solicitud?.etapaCodigo);
  }

  get tieneFormatoDecano(): boolean {
    return !!this.solicitud?.formatoFirmadoDecanoUuid;
  }

  get tieneEscaneo(): boolean {
    return !!this.solicitud?.formatoFirmadoEstudianteUuid;
  }

  alternarAjuste(): void {
    this.verAjuste = !this.verAjuste;
    if (this.verAjuste) {
      this.ajusteX = Number(this.solicitud?.ajusteFormatoXmm ?? 0);
      this.ajusteY = Number(this.solicitud?.ajusteFormatoYmm ?? 0);
      this.errorAjuste = '';
    }
  }

  verEscaneo(): void {
    const uuidAnexo = this.solicitud?.formatoFirmadoEstudianteUuid;
    if (!uuidAnexo || this.abriendoEscaneo) return;
    this.abriendoEscaneo = true;
    this.solicitudAcademicaService
      .descargarAnexo(this.uuidSolicitud, uuidAnexo)
      .pipe(finalize(() => (this.abriendoEscaneo = false)))
      .subscribe({
        next: blob => guardarArchivo(blob, `Formato-${this.solicitud.radicado}-firmado-por-el-estudiante.pdf`),
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo descargar el escaneo del estudiante')
      });
  }

  verFormatoDecano(): void {
    const uuidAnexo = this.solicitud?.formatoFirmadoDecanoUuid;
    if (!uuidAnexo || this.abriendoFormatoDecano) return;
    this.abriendoFormatoDecano = true;
    this.solicitudAcademicaService
      .descargarAnexo(this.uuidSolicitud, uuidAnexo)
      .pipe(finalize(() => (this.abriendoFormatoDecano = false)))
      .subscribe({
        next: blob => guardarArchivo(blob, `Formato-${this.solicitud.radicado}-firmado-por-el-Decano.pdf`),
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo descargar el formato firmado por el Decano')
      });
  }

  descargarCompuesto(): void {
    if (this.descargando) return;
    this.descargando = true;
    const valores = this.esFuncionario || (this.esDecano && this.firmaElDecano) ? this.valoresProvisionales() : [];
    this.solicitudAcademicaService
      .descargarFormatoCompuesto(this.uuidSolicitud, valores)
      .pipe(finalize(() => (this.descargando = false)))
      .subscribe({
        next: blob => guardarArchivo(blob, `Formato-${this.solicitud.radicado}-${this.esDecano ? 'con-la-decision' : 'para-firma'}.pdf`),
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo generar el formato')
      });
  }

  guardarAjuste(): void {
    if (this.guardandoAjuste) return;
    const x = Number(this.ajusteX);
    const y = Number(this.ajusteY);
    if (!Number.isFinite(x) || !Number.isFinite(y) || Math.abs(x) > AJUSTE_MAXIMO_MM || Math.abs(y) > AJUSTE_MAXIMO_MM) {
      this.errorAjuste = `Cada desplazamiento debe estar entre -${AJUSTE_MAXIMO_MM} y ${AJUSTE_MAXIMO_MM} milímetros.`;
      return;
    }
    this.errorAjuste = '';
    this.guardandoAjuste = true;
    this.solicitudAcademicaService
      .guardarAjusteFormato(this.uuidSolicitud, x, y)
      .pipe(finalize(() => (this.guardandoAjuste = false)))
      .subscribe({
        next: detalle => {
          this.solicitud = { ...this.solicitud, ajusteFormatoXmm: detalle.ajusteFormatoXmm, ajusteFormatoYmm: detalle.ajusteFormatoYmm };
          this.toastService.showSuccess('Ajuste guardado', 'Se descargará de nuevo el formato con la posición corregida.');
          this.descargarCompuesto();
        },
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo guardar el ajuste')
      });
  }

  alSeleccionar(archivo: File, entrada: InputAnexoUploadComponent): void {
    const error = errorDeArchivo(archivo, FORMATOS_RESOLUCION, TAMANIO_MAXIMO_PAZ_Y_SALVOS_BYTES);
    if (error) {
      this.error = error;
      this.archivo = null;
      entrada.reset();
      return;
    }
    this.error = '';
    this.archivo = archivo;
  }

  alQuitar(): void {
    this.archivo = null;
    this.error = '';
  }

  subirFormatoDecano(): void {
    if (this.subiendo) return;
    const archivo = this.archivo;
    if (!archivo) {
      this.error = 'Se debe seleccionar el formato firmado por el Decano en PDF.';
      return;
    }
    this.subiendo = true;
    this.catalogoAcademicoService
      .getTiposAnexoPorTipo(this.solicitud.uuidTipoSolicitudAcademica)
      .pipe(
        switchMap(tipos => {
          const tipo = (tipos ?? []).find(t => normalizado(t.nombre) === normalizado(FORMATO_FIRMADO_DECANO));
          if (!tipo) return throwError(() => ({ message: `El catálogo no tiene el tipo de anexo ${FORMATO_FIRMADO_DECANO}` }));
          return this.solicitudAcademicaService.adjuntarAnexo(this.uuidSolicitud, { archivo, tipoAnexo: tipo.uuidTipoAnexoAcademico });
        }),
        map(() => undefined),
        finalize(() => (this.subiendo = false))
      )
      .subscribe({
        next: () => {
          this.archivo = null;
          this.toastService.showSuccess('Formato cargado', 'El formato firmado por el Decano quedó en la solicitud.');
          this.formatoSubido.emit();
        },
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No se pudo subir el formato firmado por el Decano')
      });
  }
}
