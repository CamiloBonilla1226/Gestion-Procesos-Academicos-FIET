import { Component, Input } from '@angular/core';
import { finalize } from 'rxjs';
import { SimpleButtonComponent } from '../../buttons/simple-button-component/simple-button-component';
import { CancelacionAsignaturaService } from '../../../core/services/cancelacion-asignatura-service';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';
import {
  BorradorResolucionDTORespuesta,
  FilaResolucionDTO
} from '../../../core/models/CancelacionAsignatura/DTOResponse/BorradorResolucionDTORespuesta';
import { BorradorResolucionDTOPeticion } from '../../../core/models/CancelacionAsignatura/DTORequest/BorradorResolucionDTOPeticion';
import { AjusteAsignaturaDTOPeticion } from '../../../core/models/CancelacionAsignatura/DTORequest/AjusteAsignaturaDTOPeticion';
import { abrirVentana, guardarArchivo, mostrarArchivo } from '../../../core/utils/descargas';
import {
  MAXIMO_CAMPO_RESOLUCION,
  MAXIMO_CELDA_RESOLUCION,
  MAXIMO_NUMERO_RESOLUCION,
  MAXIMO_TEXTO_RESOLUCION,
  erroresDeResolucion
} from '../../../core/utils/validaciones-resolucion';

type CampoTexto =
  | 'numero'
  | 'fecha'
  | 'asunto'
  | 'preambulo'
  | 'considerandos'
  | 'articuloPrimero'
  | 'articulosSiguientes'
  | 'firmante'
  | 'cargoFirmante'
  | 'diligencia'
  | 'proyecto'
  | 'reviso';

type CampoFila = keyof FilaResolucionDTO;

@Component({
  selector: 'app-resoluciones-asignatura-component',
  imports: [SimpleButtonComponent],
  templateUrl: './resoluciones-asignatura-component.html',
  styleUrls: ['../../pages/content/estilos-academicos.css', './resoluciones-asignatura-component.css']
})
export class ResolucionesAsignaturaComponent {
  @Input({ required: true }) uuidSolicitud!: string;
  @Input({ required: true }) radicado!: string;
  @Input() ajustes: () => AjusteAsignaturaDTOPeticion[] = () => [];
  @Input() asignaturasSinSituaciones = 0;

  readonly maximoTexto = MAXIMO_TEXTO_RESOLUCION;
  readonly maximoCampo = MAXIMO_CAMPO_RESOLUCION;
  readonly maximoCelda = MAXIMO_CELDA_RESOLUCION;
  readonly maximoNumero = MAXIMO_NUMERO_RESOLUCION;

  readonly columnas: { campo: CampoFila; titulo: string }[] = [
    { campo: 'asignatura', titulo: 'Asignatura' },
    { campo: 'faltas', titulo: 'No. faltas' },
    { campo: 'nota', titulo: 'Nota' },
    { campo: 'estadoAlSolicitar', titulo: 'Estado al momento de la solicitud' },
    { campo: 'estadoCancelacion', titulo: 'Estado de la cancelación' },
    { campo: 'estadoNuevaMatricula', titulo: 'Estado cuando la matricule de nuevo' }
  ];

  borradores: BorradorResolucionDTORespuesta[] = [];
  cargando = false;
  generando: Record<number, 'ver' | 'descargar' | null> = {};
  errores: Record<number, Record<string, string>> = {};
  editado = false;
  confirmarRegenerar = false;

  constructor(
    private cancelacionAsignaturaService: CancelacionAsignaturaService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  get hayBorradores(): boolean {
    return this.borradores.length > 0;
  }

  pedirGeneracion(): void {
    if (this.cargando) return;
    if (this.hayBorradores && this.editado) {
      this.confirmarRegenerar = true;
      return;
    }
    this.generar();
  }

  cancelarRegeneracion(): void {
    this.confirmarRegenerar = false;
  }

  generar(): void {
    if (this.cargando) return;
    this.confirmarRegenerar = false;
    this.cargando = true;
    this.cancelacionAsignaturaService
      .getBorradoresResolucion(this.uuidSolicitud, { ajustes: this.ajustes() })
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: borradores => {
          this.borradores = borradores.map(borrador => ({ ...borrador, filas: (borrador.filas ?? []).map(fila => ({ ...fila })) }));
          this.generando = {};
          this.errores = {};
          this.editado = false;
        },
        error: err => this.errorHandlerService.handleError(err, 'Error', 'No fue posible generar las resoluciones')
      });
  }

  cambiar(borrador: BorradorResolucionDTORespuesta, campo: CampoTexto, evento: Event): void {
    borrador[campo] = (evento.target as HTMLInputElement | HTMLTextAreaElement).value;
    this.editado = true;
    this.limpiarError(borrador, campo);
  }

  error(indice: number, clave: string): string {
    return this.errores[indice]?.[clave] ?? '';
  }

  cantidadErrores(indice: number): number {
    return Object.keys(this.errores[indice] ?? {}).length;
  }

  cambiarFila(fila: FilaResolucionDTO, campo: CampoFila, evento: Event): void {
    fila[campo] = (evento.target as HTMLInputElement).value;
    this.editado = true;
  }

  ver(indice: number): void {
    const ventana = abrirVentana();
    this.pedirPdf(indice, 'ver', blob => mostrarArchivo(ventana, blob, this.nombreArchivo(indice)), () => ventana?.close());
  }

  descargar(indice: number): void {
    this.pedirPdf(indice, 'descargar', blob => guardarArchivo(blob, this.nombreArchivo(indice)));
  }

  titulo(borrador: BorradorResolucionDTORespuesta, indice: number): string {
    return `Resolución ${indice + 1} de ${this.borradores.length}: ${borrador.asignatura}`;
  }

  private pedirPdf(indice: number, accion: 'ver' | 'descargar', alListo: (blob: Blob) => void, alFallar?: () => void): void {
    const borrador = this.borradores[indice];
    if (!borrador || this.generando[indice]) {
      alFallar?.();
      return;
    }
    const errores = erroresDeResolucion(borrador, accion === 'descargar');
    this.errores = { ...this.errores, [indice]: errores };
    if (Object.keys(errores).length > 0) {
      alFallar?.();
      return;
    }
    this.generando[indice] = accion;
    this.cancelacionAsignaturaService
      .generarResolucion(this.uuidSolicitud, this.peticionDe(borrador))
      .pipe(finalize(() => (this.generando[indice] = null)))
      .subscribe({
        next: alListo,
        error: err => {
          alFallar?.();
          this.errorHandlerService.handleError(err, 'Error', 'No fue posible generar el PDF de la Resolución');
        }
      });
  }

  private limpiarError(borrador: BorradorResolucionDTORespuesta, campo: CampoTexto): void {
    const indice = this.borradores.indexOf(borrador);
    if (indice < 0 || !this.errores[indice]?.[campo]) return;
    const restantes = { ...this.errores[indice] };
    delete restantes[campo];
    this.errores = { ...this.errores, [indice]: restantes };
  }

  private peticionDe(borrador: BorradorResolucionDTORespuesta): BorradorResolucionDTOPeticion {
    return {
      asignaturaSolicitudUuid: borrador.asignaturaSolicitudUuid,
      numero: borrador.numero,
      fecha: borrador.fecha,
      asunto: borrador.asunto,
      preambulo: borrador.preambulo,
      considerandos: borrador.considerandos,
      articuloPrimero: borrador.articuloPrimero,
      filas: borrador.filas,
      articulosSiguientes: borrador.articulosSiguientes,
      firmante: borrador.firmante,
      cargoFirmante: borrador.cargoFirmante,
      diligencia: borrador.diligencia,
      proyecto: borrador.proyecto,
      reviso: borrador.reviso
    };
  }

  private nombreArchivo(indice: number): string {
    const borrador = this.borradores[indice];
    const numero = (borrador?.numero ?? '').replace(/[^0-9A-Za-z-]/g, '');
    return `Resolucion-${this.radicado}-${numero || indice + 1}.pdf`;
  }
}
