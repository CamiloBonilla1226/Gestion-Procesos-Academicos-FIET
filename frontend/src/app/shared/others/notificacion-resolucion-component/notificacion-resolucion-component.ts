import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Observable, finalize } from 'rxjs';
import { GenericDialogFormComponent } from '../../generic-dialog-form-component/generic-dialog-form-component';
import { SimpleButtonComponent } from '../../buttons/simple-button-component/simple-button-component';
import { InputAnexoUploadComponent } from '../../inputs/input-anexo-upload-component/input-anexo-upload-component';
import { ResumenSolicitudComponent } from '../resumen-solicitud-component/resumen-solicitud-component';
import { ProgramacionResolucionComponent } from '../programacion-resolucion-component/programacion-resolucion-component';
import { TextoComunicacionComponent } from '../texto-comunicacion-component/texto-comunicacion-component';
import { CancelacionAsignaturaService } from '../../../core/services/cancelacion-asignatura-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import { ToastService } from '../../../core/services/toast-service';
import { SolicitudAcademicaDetalleDTORespuesta } from '../../../core/models/SolicitudAcademica/DTOResponse/SolicitudAcademicaDetalleDTORespuesta';
import { NotificacionResolucionDTORespuesta } from '../../../core/models/CancelacionAsignatura/DTOResponse/NotificacionResolucionDTORespuesta';
import { FORMATOS_RESOLUCION } from '../../../core/constantes/procesos-academicos';
import { comoTipoAnexo, errorDeArchivo } from '../../../core/utils/validaciones-academicas';
import {
  ESTADOS_NOTIFICACION,
  ProgramacionEnEdicion,
  ahoraEnColombia,
  errorDeTextoComunicacion,
  erroresDeProgramacion,
  fechaCarta,
  programacionAPeticion,
  programacionDesde,
  textoFechaHora
} from '../../../core/utils/notificacion-resolucion';

type DialogoNotificacion = 'reprogramar' | 'firma' | 'reintentar';

const RESOLUCION_SIN_FIRMAR = 'Sin firmar por el estudiante';
const RESOLUCION_FIRMADA = 'Firmada por el estudiante';
const ESTADO_CERRADA = 'Cerrada por la firma del estudiante';

const TITULOS: Record<DialogoNotificacion, string> = {
  reprogramar: 'Reprogramar la comunicación de la Resolución',
  firma: 'Registrar que el estudiante firmó',
  reintentar: 'Reintentar el envío'
};

const BOTONES: Record<DialogoNotificacion, string> = {
  reprogramar: 'Guardar cambios',
  firma: 'Registrar firma',
  reintentar: 'Reintentar'
};

const EXITOS: Record<DialogoNotificacion, string> = {
  reprogramar: 'La comunicación quedó reprogramada.',
  firma: 'La comunicación quedó cerrada: DARCA recibe ahora la Resolución firmada y el estudiante ya ve la respuesta.',
  reintentar: 'El envío quedó programado de nuevo.'
};

@Component({
  selector: 'app-notificacion-resolucion-component',
  imports: [
    GenericDialogFormComponent,
    SimpleButtonComponent,
    InputAnexoUploadComponent,
    ResumenSolicitudComponent,
    ProgramacionResolucionComponent,
    TextoComunicacionComponent
  ],
  templateUrl: './notificacion-resolucion-component.html',
  styleUrl: '../../pages/content/estilos-academicos.css'
})
export class NotificacionResolucionComponent implements OnChanges {
  @Input({ required: true }) uuidSolicitud!: string;
  @Input({ required: true }) solicitud!: SolicitudAcademicaDetalleDTORespuesta;
  @Output() cambio = new EventEmitter<void>();

  readonly tipoFirma = comoTipoAnexo('resolucion-firmada', 'Resolución firmada por el estudiante', FORMATOS_RESOLUCION, true);

  notificacion: NotificacionResolucionDTORespuesta | null = null;
  cargando = false;
  errorCarga = false;
  dialogo: DialogoNotificacion | null = null;
  enviando = false;
  programacion: ProgramacionEnEdicion = { fecha: '', numero: '', anio: '', decision: '' };
  archivoFirma: File | null = null;
  textoDarca = '';
  readonly ayudaDarca = '{fecha} se reemplaza por la fecha de hoy, cuando sale el correo.';
  errores: Record<string, string> = {};

  constructor(
    private cancelacionAsignaturaService: CancelacionAsignaturaService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService
  ) {}

  ngOnChanges(cambios: SimpleChanges): void {
    if (cambios['uuidSolicitud'] || cambios['solicitud']) this.cargar();
  }

  get estado(): string {
    if (!this.notificacion) return '';
    const envio = ESTADOS_NOTIFICACION[this.notificacion.estado] ?? this.notificacion.estado;
    return this.notificacion.firmada ? `${ESTADO_CERRADA} (correo a DARCA: ${envio.toLowerCase()})` : envio;
  }

  get valoresDarca(): Record<string, string> {
    return { fecha: fechaCarta(ahoraEnColombia()) };
  }

  get resolucion(): string {
    const n = this.notificacion;
    return n ? `No. ${n.numeroResolucion}${n.sufijoResolucion ?? ''} de ${n.anioResolucion}` : '';
  }

  get resolucionVigente(): string {
    return this.notificacion?.firmada ? RESOLUCION_FIRMADA : RESOLUCION_SIN_FIRMAR;
  }

  get puedeReintentar(): boolean {
    return this.notificacion?.estado === 'FALLIDA';
  }

  get hayAcciones(): boolean {
    return !!this.notificacion && (this.notificacion.editable || this.puedeReintentar);
  }

  get tituloDialogo(): string {
    return this.dialogo ? TITULOS[this.dialogo] : '';
  }

  get textoBoton(): string {
    return this.dialogo ? BOTONES[this.dialogo] : 'Confirmar';
  }

  fecha(valor: string | null): string {
    return textoFechaHora(valor);
  }

  cargar(): void {
    if (!this.uuidSolicitud) return;
    this.cargando = true;
    this.errorCarga = false;
    this.cancelacionAsignaturaService
      .getNotificacion(this.uuidSolicitud)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: notificacion => (this.notificacion = notificacion ?? null),
        error: err => {
          this.notificacion = null;
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar la notificación de la Resolución');
        }
      });
  }

  abrir(dialogo: DialogoNotificacion): void {
    if (!this.notificacion || this.enviando) return;
    if (dialogo !== 'reintentar' && !this.notificacion.editable) return;
    if (dialogo === 'reintentar' && !this.puedeReintentar) return;
    this.dialogo = dialogo;
    this.errores = {};
    this.archivoFirma = null;
    this.textoDarca = this.notificacion.textoDarcaFirma ?? this.notificacion.textoDarca ?? '';
    this.programacion = programacionDesde(this.notificacion);
  }

  cerrar(visible: boolean): void {
    if (visible || this.enviando) return;
    this.dialogo = null;
  }

  alSeleccionarFirma(archivo: File, entrada: InputAnexoUploadComponent): void {
    const error = errorDeArchivo(archivo, FORMATOS_RESOLUCION);
    if (error) {
      this.archivoFirma = null;
      this.errores['firma'] = error;
      entrada.reset();
      return;
    }
    this.archivoFirma = archivo;
    delete this.errores['firma'];
  }

  alQuitarFirma(): void {
    this.archivoFirma = null;
  }

  confirmar(): void {
    if (this.enviando || !this.dialogo) return;
    const dialogo = this.dialogo;
    this.errores = this.validar(dialogo);
    if (Object.keys(this.errores).length > 0) return;
    this.enviando = true;
    this.ejecutar(dialogo)
      .pipe(finalize(() => (this.enviando = false)))
      .subscribe({
        next: notificacion => {
          this.notificacion = notificacion;
          this.dialogo = null;
          this.toastService.showSuccess('Listo', EXITOS[dialogo]);
          this.cambio.emit();
        },
        error: err => {
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo completar la acción');
          this.cargar();
        }
      });
  }

  private validar(dialogo: DialogoNotificacion): Record<string, string> {
    if (dialogo === 'reprogramar') return erroresDeProgramacion(this.programacion);
    if (dialogo !== 'firma') return {};
    const errores: Record<string, string> = {};
    if (!this.archivoFirma) errores['firma'] = 'Sube la Resolución firmada por el estudiante (PDF).';
    const errorTexto = errorDeTextoComunicacion(this.textoDarca, 'DARCA');
    if (errorTexto) errores['textoDarca'] = errorTexto;
    return errores;
  }

  private ejecutar(dialogo: DialogoNotificacion): Observable<NotificacionResolucionDTORespuesta> {
    if (dialogo === 'reprogramar')
      return this.cancelacionAsignaturaService.reprogramarNotificacion(this.uuidSolicitud, programacionAPeticion(this.programacion));
    if (dialogo === 'firma')
      return this.cancelacionAsignaturaService.registrarFirma(this.uuidSolicitud, this.archivoFirma!, this.textoDarca.trim());
    return this.cancelacionAsignaturaService.reintentarNotificacion(this.uuidSolicitud);
  }
}
