import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { InputTextTareaComponent } from '../../../inputs/input-text-tarea-component/input-text-tarea-component';
import { SimpleButtonComponent } from '../../../buttons/simple-button-component/simple-button-component';
import { CancelacionAsignaturaService } from '../../../../core/services/cancelacion-asignatura-service';
import { ErrorHandlerService } from '../../../../core/services/error-handler-service';
import { ToastService } from '../../../../core/services/toast-service';
import { RadicacionCancelacionAsignaturaDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/RadicacionCancelacionAsignaturaDTORespuesta';
import { FormularioCancelacionAsignaturaDTORespuesta } from '../../../../core/models/CancelacionAsignatura/DTOResponse/FormularioCancelacionAsignaturaDTORespuesta';
import {
  MAXIMO_CARACTERES_MOTIVO,
  RUTAS_SOLICITUDES_POR_ROL,
  ROLES_ETIQUETA,
  TAMANIO_MAXIMO_ARCHIVO_BYTES
} from '../../../../core/constantes/procesos-academicos';
import {
  ARTICULO_5_CANCELACION_ASIGNATURA,
  NOTIFICACION_CORREO_INSTITUCIONAL
} from '../../../../core/constantes/textos-cancelacion-asignatura';
import { errorDeTexto, erroresDeCampos } from '../../../../core/utils/validaciones-academicas';
import { DatosEstudianteSesionComponent } from '../../../others/datos-estudiante-sesion-component/datos-estudiante-sesion-component';
import { FormatoEstudianteComponent } from '../../../others/formato-estudiante-component/formato-estudiante-component';
import { PasoFormulario, StepperFormularioComponent } from '../../../others/stepper-formulario-component/stepper-formulario-component';

@Component({
  selector: 'app-est-cancelacion-asignatura-content-component',
  imports: [
    FormatoEstudianteComponent,
    InputTextTareaComponent,
    SimpleButtonComponent,
    DatosEstudianteSesionComponent,
    StepperFormularioComponent
  ],
  templateUrl: './est-cancelacion-asignatura-content-component.html',
  styleUrls: ['../estilos-academicos.css', '../estilos-formularios.css']
})
export class EstCancelacionAsignaturaContentComponent implements OnInit {
  readonly maximoMotivo = MAXIMO_CARACTERES_MOTIVO;
  readonly articulo5 = ARTICULO_5_CANCELACION_ASIGNATURA;
  readonly notificacion = NOTIFICACION_CORREO_INSTITUCIONAL;

  formulario: FormularioCancelacionAsignaturaDTORespuesta | null = null;
  cargando = false;
  errorCarga = false;

  motivo = '';
  seleccionadas = new Set<string>();
  maximoSoporteBytes = TAMANIO_MAXIMO_ARCHIVO_BYTES;
  nombreSoporte = 'Soporte del motivo de la cancelación';

  errores: Record<string, string> = {};
  erroresServidor: Record<string, string> = {};
  intentoEnvio = false;
  enviando = false;
  radicacion: RadicacionCancelacionAsignaturaDTORespuesta | null = null;

  constructor(
    private cancelacionAsignaturaService: CancelacionAsignaturaService,
    private errorHandlerService: ErrorHandlerService,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarFormulario();
  }

  get puedeRadicar(): boolean {
    return !!this.formulario && this.formulario.asignaturas.length > 0;
  }

  get pasos(): PasoFormulario[] {
    const errores = this.validar();
    return [
      { titulo: 'Datos del estudiante', completo: true },
      { titulo: 'Asignaturas', completo: !errores['asignaturas'] },
      { titulo: 'Motivo', completo: !errores['motivo'] }
    ];
  }

  get erroresServidorGenerales(): string[] {
    const conocidos = new Set(['motivo', 'asignaturas']);
    return Object.entries(this.erroresServidor)
      .filter(([campo]) => !conocidos.has(campo))
      .map(([campo, mensaje]) => `${campo}: ${mensaje}`);
  }

  cargarFormulario(): void {
    this.cargando = true;
    this.errorCarga = false;
    this.cancelacionAsignaturaService
      .getFormulario()
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: formulario => {
          this.formulario = formulario;
          const soporte = formulario.soportes?.[0];
          if (soporte) {
            this.maximoSoporteBytes = soporte.tamanioMaximoBytes || TAMANIO_MAXIMO_ARCHIVO_BYTES;
          }
        },
        error: err => {
          this.errorCarga = true;
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo cargar el formulario');
        }
      });
  }

  estaSeleccionada(uuid: string): boolean {
    return this.seleccionadas.has(uuid);
  }

  alCambiarSeleccion(uuid: string, evento: Event): void {
    const marcada = (evento.target as HTMLInputElement).checked;
    const nuevas = new Set(this.seleccionadas);
    if (marcada) nuevas.add(uuid);
    else nuevas.delete(uuid);
    this.seleccionadas = nuevas;
    this.revalidar();
  }

  alCambiarMotivo(valor: string): void {
    this.motivo = valor ?? '';
    this.revalidar();
  }

  generarSolicitud(): void {
    if (this.enviando || !this.puedeRadicar) return;
    this.intentoEnvio = true;
    this.errores = this.validar();
    if (Object.keys(this.errores).length > 0) {
      this.toastService.showError('Formulario incompleto', 'Hay campos pendientes o con errores.');
      return;
    }
    this.enviando = true;
    this.erroresServidor = {};
    const asignaturas = this.formulario!.asignaturas
      .map(a => a.uuidAsignaturaMatriculada)
      .filter(uuid => this.seleccionadas.has(uuid));
    this.cancelacionAsignaturaService
      .radicar({ motivo: this.motivo.trim(), asignaturas, soportes: [] })
      .pipe(finalize(() => (this.enviando = false)))
      .subscribe({
        next: respuesta => {
          this.radicacion = respuesta;
          this.toastService.showSuccess('Solicitud generada', `Radicado ${respuesta.radicado}: se descarga el formato para firmarlo`);
        },
        error: err => {
          this.erroresServidor = erroresDeCampos(err);
          this.errorHandlerService.handleError(err, 'Error', 'No se pudo generar la solicitud');
        }
      });
  }

  volver(): void {
    this.router.navigate([RUTAS_SOLICITUDES_POR_ROL[ROLES_ETIQUETA.ESTUDIANTE]]);
  }

  irAlDetalle(): void {
    if (!this.radicacion) return;
    this.router.navigate([RUTAS_SOLICITUDES_POR_ROL[ROLES_ETIQUETA.ESTUDIANTE], this.radicacion.uuidSolicitudAcademica]);
  }

  private revalidar(): void {
    if (this.intentoEnvio) this.errores = this.validar();
  }

  private validar(): Record<string, string> {
    const errores: Record<string, string> = {};
    const errorMotivo = errorDeTexto(this.motivo, MAXIMO_CARACTERES_MOTIVO, true, 'El motivo de la cancelación');
    if (errorMotivo) errores['motivo'] = errorMotivo;
    if (this.seleccionadas.size === 0) errores['asignaturas'] = 'Se debe seleccionar al menos una asignatura para cancelar.';
    return errores;
  }
}
