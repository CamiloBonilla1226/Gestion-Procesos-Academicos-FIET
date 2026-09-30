import { Component, Input, Output, EventEmitter, ViewChild, OnInit } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { SimpleButtonComponent } from '../../../../shared/buttons/simple-button-component/simple-button-component';
import { SECCIONES_ORDEN_DIA, TIPOS_RESPUESTAS } from '../../../constantes/constantes';
import { OrdenDelDiaDTORespuesta } from '../../../models/Solicitudes/DTOResponse/OrdenDelDiaDTORespuesta';
import { SolicitudesService } from '../../../services/solicitudes-service';
import { SolicitudDTORespuesta } from '../../../models/Solicitudes/DTOResponse/SolicitudDTORespuesta';
import { ButtonComponent } from '../../../../shared/buttons/button-component/button-component';
import { GenericDialogInfoComponent } from '../../../../shared/generic-dialog-info-component/generic-dialog-info-component';
import { AnexosViewComponent } from '../../../../shared/others/anexos-view-component/anexos-view-component';
import { InputSelectComponent } from '../../../../shared/inputs/input-select-component/input-select-component';
import { PENDIENTE_AL_ORDEN_DEL_DIA } from '../../../constantes/constantes';
import { SolicitudActualizarDTOPeticion } from '../../../models/Solicitudes/DTORequest/SolicitudActualizarDTOPeticion';
import { AGREGADO_EN_EL_ORDEN_DEL_DIA } from '../../../constantes/constantes';
import { ToastService } from '../../../services/toast-service';
import { ErrorHandlerService } from '../../../services/error-handler-service';
import { SimpleButtonGroupComponent } from '../../../../shared/buttons/simple-button-group-component/simple-button-group-component';
import { RespuestaDTOPeticion } from '../../../models/Respuesta/DTORequest/RespuestaDTOPeticion';
import { InputTextComponent } from '../../../../shared/inputs/input-text-component/input-text-component';
import { InputTextTareaComponent } from '../../../../shared/inputs/input-text-tarea-component/input-text-tarea-component';
import { RespuestasService } from '../../../services/respuesta-service';
import { GenericDialogFormComponent } from '../../../../shared/generic-dialog-form-component/generic-dialog-form-component';

/**
 * Componente visualizador del Orden del Día.
 * Permite consultar, agregar, remover y exportar solicitudes asociadas a un orden del día.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-orden-del-dia-visualizador-component',
  imports: [
    CommonModule,
    DialogModule,
    ButtonModule,
    SimpleButtonComponent,
    ButtonComponent,
    GenericDialogInfoComponent,
    AnexosViewComponent,
    InputSelectComponent,
    ButtonComponent,
    SimpleButtonGroupComponent,
    GenericDialogFormComponent,
    InputTextComponent,
    InputTextTareaComponent,
  ],
  templateUrl: './orden-del-dia-visualizador-component.html',
  styleUrl: './orden-del-dia-visualizador-component.css',
})
export class OrdenDelDiaVisualizadorComponent {
  /**
   * Título principal del componente.
   */
  title: string = 'Orden del Día';

  /**
   * Secciones configuradas para el orden del día.
   */
  seccionesOrdenDia = SECCIONES_ORDEN_DIA;

  /**
   * Listado general de solicitudes cargadas.
   */
  solicitudes: any[] = [];

  /**
   * Indica si las solicitudes están siendo cargadas.
   */
  loadingSolicitudes = false;

  /**
   * Controla la visibilidad del diálogo de información de la solicitud.
   */
  solicitudInfoVisible = false;

  // Diálogo de edición de solicitud / asignar respuesta
  respuestaFormDialogVisible = false;

  selectedRespuestaForm: RespuestaDTOPeticion = {
    tipoRespuesta: '',
    consecutivoFiet: '',
    respuestaConsejo: '',
    indicaciones: '',
  };

  /**
   * Información seleccionada de la solicitud.
   */
  solicitudInfoSeleccionada: any = null;

  /**
   * Información detallada de la solicitud seleccionada.
   */
  selectedSolicitudInfo: any = null;

  /**
   * Listado de anexos de la solicitud seleccionada.
   */
  solicitudAnexos: any[] = [];

  /**
   * Indica si el diálogo utiliza un componente adicional.
   */
  dialogUsaComponente = false;

  /**
   * Opciones disponibles de solicitudes para agregar al orden del día.
   */
  solicitudesDisponiblesOpciones: { label: string; value: string }[] = [];

  /**
   * UUID de la solicitud seleccionada para ser agregada.
   */
  solicitudSeleccionadaParaAgregar: string | null = null;

  /** /
   * Tipos de Respuesta.
   */
  tiposRespuesta = TIPOS_RESPUESTAS;

  /**
   * Referencia al componente select de solicitudes.
   */
  @ViewChild('inputSolicitudAgregar') inputSolicitudAgregar!: InputSelectComponent;

  /**
   * Referencia al componente select del tipo de respuesta.
   */
  @ViewChild('tipoRespuesta') inputTipoRespuesta!: InputSelectComponent;

  /**
   * Referencia al componente select del consecutivo fiet.
   */
  @ViewChild('consecutivoFiet') inputConsecutivo!: InputTextComponent;

  /**
   * Referencia al componente select del consecutivo fiet.
   */
  @ViewChild('respuestaConsejo') inputRespuestaConsejo!: InputTextTareaComponent;

  /**
   * Referencia al componente select del consecutivo fiet.
   */
  @ViewChild('indicaciones') inputIndicaciones!: InputTextTareaComponent;

  /**
   * Solicitudes agrupadas por sección del orden del día.
   */
  solicitudesPorSeccion: Record<string, SolicitudDTORespuesta[]> = SECCIONES_ORDEN_DIA.reduce(
    (acc, s) => {
      acc[s.label] = [];
      return acc;
    },
    {} as Record<string, SolicitudDTORespuesta[]>
  );

  /**
   * Controla la visibilidad externa del componente.
   */
  @Input() visible: boolean = false;

  /**
   * Orden del día actual.
   */
  private _orden!: OrdenDelDiaDTORespuesta;

  /**
   * Asigna el orden del día y carga sus solicitudes.
   * @param value Orden del día recibido
   */
  @Input()
  set orden(value: OrdenDelDiaDTORespuesta) {
    if (value?.uuidOrdenDelDia) {
      this._orden = value;
      this.cargarSolicitudes();
    }
  }

  /**
   * Retorna el orden del día actual.
   */
  get orden(): OrdenDelDiaDTORespuesta {
    return this._orden;
  }

  /**
   * Evento que notifica el cambio de visibilidad del componente.
   */
  @Output() visibleChange = new EventEmitter<boolean>();

  constructor(
    private solicitudesService: SolicitudesService,
    private toastService: ToastService,
    private handlerError: ErrorHandlerService,
    private respuestaService: RespuestasService
  ) {}

  /**
   * Inicializa las secciones del orden del día.
   */
  private inicializarSecciones(): void {
    this.solicitudesPorSeccion = this.seccionesOrdenDia.reduce((acc, s) => {
      acc[s.label] = [];
      return acc;
    }, {} as Record<string, SolicitudDTORespuesta[]>);
  }

  /**
   * Carga las solicitudes asociadas al orden del día.
   */
  cargarSolicitudes(): void {
    this.loadingSolicitudes = true;
    this.solicitudes = [];
    this.inicializarSecciones();

    this.solicitudesService.getSolicitudesPorOrdenDelDia(this._orden.uuidOrdenDelDia).subscribe({
      next: (data) => {
        this.solicitudes = [...data];
        this.agruparSolicitudesPorSeccion();
        this.loadingSolicitudes = false;
      },
      error: () => {
        this.solicitudes = [];
        this.inicializarSecciones();
        this.loadingSolicitudes = false;
      },
    });
  }

  /**
   * Carga las solicitudes disponibles para agregar al orden del día.
   */
  cargarSolicitudesDisponibles(): void {
    this.solicitudesService.getSolicitudesPorEstado(PENDIENTE_AL_ORDEN_DEL_DIA).subscribe({
      next: (data) => {
        this.solicitudesDisponiblesOpciones = data.map((s) => ({
          label: s.nombre,
          value: s.uuidSolicitud,
        }));
      },
      error: (err) => {
        this.solicitudesDisponiblesOpciones = [];
      },
    });
  }

  /**
   * Agrupa las solicitudes según la sección a la que pertenecen.
   */
  agruparSolicitudesPorSeccion(): void {
    this.inicializarSecciones();

    for (const solicitud of this.solicitudes) {
      const seccion = solicitud.tipoSolicitud?.seccion;
      if (seccion && this.solicitudesPorSeccion[seccion])
        this.solicitudesPorSeccion[seccion] = [...this.solicitudesPorSeccion[seccion], solicitud];
    }
  }

  /**
   * Consulta y muestra la información detallada de una solicitud.
   * @param solicitud Solicitud seleccionada
   */
  verSolicitud(solicitud: SolicitudDTORespuesta): void {
    this.solicitudesService.getSolicitud(solicitud.uuidSolicitud).subscribe({
      next: (solicitudDetallada) => {
        const tieneAnexos = solicitudDetallada.anexos?.length > 0;

        this.selectedSolicitudInfo = {
          Nombre: solicitudDetallada.nombre || 'N/A',
          Descripción: solicitudDetallada.descripcion,
          Estado: solicitudDetallada.estado,
          Consecutivo: solicitudDetallada.consecutivo || 'Sin consecutivo',
          Orden_del_Día: solicitudDetallada.ordenDelDia
            ? solicitudDetallada.ordenDelDia.nombre
            : 'Sin orden del día',
          Tipo_de_Solicitud: solicitudDetallada.tipoSolicitud?.nombre,
          Solicitante:
            solicitudDetallada.informacionSolicitante?.nombres +
            ' ' +
            solicitudDetallada.informacionSolicitante?.apellidos,
          Identificación:
            solicitudDetallada.informacionSolicitante?.tipoDocumento +
            ' ' +
            solicitudDetallada.informacionSolicitante?.numeroDocumento,
          Contacto:
            solicitudDetallada.informacionSolicitante?.correoElectronico +
            ' / ' +
            solicitudDetallada.informacionSolicitante?.telefono,
        };

        this.solicitudAnexos = solicitudDetallada.anexos || [];
        this.dialogUsaComponente = tieneAnexos;
        this.solicitudInfoVisible = true;
      },
    });
  }

  /**
   * Abre el formulario para agregar o modificar la información de la respuesta
   * @param solicitud solicitud a asignar o modificar respuesta.
   */
  abrirFormularioRespuesta(solicitud: SolicitudDTORespuesta) {
    this.solicitudInfoSeleccionada = solicitud; // ← CLAVE

    this.inputTipoRespuesta.reset();
    this.inputConsecutivo.reset();
    this.inputRespuestaConsejo.reset();

    this.selectedRespuestaForm = {
      tipoRespuesta: '',
      consecutivoFiet: '',
      respuestaConsejo: '',
      indicaciones: '',
    };

    this.respuestaService.getRespuestaPorSolicitud(solicitud.uuidSolicitud).subscribe({
      next: (res) => {
        this.selectedRespuestaForm = {
          tipoRespuesta: res.tipoRespuesta,
          consecutivoFiet: res.consecutivoFiet,
          respuestaConsejo: res.respuestaConsejo,
          indicaciones: res.indicaciones,
        };
      },
      error: () => {},
    });

    this.respuestaFormDialogVisible = true;
  }

  /**
   * Método pendiente para modificar una solicitud.
   * @param solicitud Solicitud a modificar
   */
  modificarSolicitud(): void {
    this.inputTipoRespuesta.touched = true;
    this.inputConsecutivo.touched = true;
    this.inputRespuestaConsejo.touched = true;

    if (
      this.inputTipoRespuesta.isInvalid() ||
      this.inputConsecutivo.isInvalid() ||
      this.inputRespuestaConsejo.isInvalid()
    ) {
      this.toastService.showError('Error', 'Completa todos los campos requeridos.');
      return;
    }

    this.respuestaService
      .registrarRespuesta(this.solicitudInfoSeleccionada.uuidSolicitud, this.selectedRespuestaForm)
      .subscribe({
        next: (res) => {
          this.toastService.showSuccess('Éxito', 'Respuesta guardada correctamente');
          this.respuestaFormDialogVisible = false;
        },
        error: (err) => {
          this.handlerError.handleError(err, 'Error al guardar la respuesta');
        },
      });
  }

  /**
   * Remueve una solicitud del orden del día.
   * @param solicitud Solicitud a remover
   */
  removerSolicitud(solicitud: SolicitudDTORespuesta): void {
    const peticion: SolicitudActualizarDTOPeticion = {
      consecutivo: solicitud.consecutivo || '',
      nombre: solicitud.nombre,
      descripcion: solicitud.descripcion || '',
      estado: PENDIENTE_AL_ORDEN_DEL_DIA,
      uuidFuncionario: solicitud.objFuncionario.uuidUsuario,
      uuidOrdenDelDia: '',
    };

    this.solicitudesService.actualizarSolicitud(solicitud.uuidSolicitud, peticion).subscribe({
      next: (res) => {
        const seccion = solicitud.tipoSolicitud?.seccion;
        if (seccion && this.solicitudesPorSeccion[seccion]) {
          this.solicitudesPorSeccion[seccion] = this.solicitudesPorSeccion[seccion].filter(
            (s) => s.uuidSolicitud !== solicitud.uuidSolicitud
          );
        }
        this.toastService.showSuccess(
          'Éxito',
          'Solicitud removida del Orden del Día correctamente'
        );
      },
      error: (err) => {
        this.handlerError.handleError(err, 'Error al remover la solicitud del Orden del Día');
      },
    });
  }

  /**
   * Captura la solicitud seleccionada desde el select.
   * @param uuidSolicitud Identificador de la solicitud
   */
  onSolicitudSeleccionada(uuidSolicitud: string): void {
    this.solicitudSeleccionadaParaAgregar = uuidSolicitud;
  }

  /**
   * Agrega la solicitud seleccionada al orden del día.
   */
  agregarSolicitudSeleccionada(): void {
    if (!this.solicitudSeleccionadaParaAgregar) return;

    const uuid = this.solicitudSeleccionadaParaAgregar;
    this.solicitudesService.getSolicitud(uuid).subscribe({
      next: (solicitudCompleta) => {
        const seccion = solicitudCompleta.tipoSolicitud?.seccion;
        if (seccion && this.solicitudesPorSeccion[seccion])
          this.solicitudesPorSeccion[seccion].push(solicitudCompleta);
        this.registrarSolicitudesEnOrdenDelDia(solicitudCompleta);
        this.solicitudSeleccionadaParaAgregar = null;
        this.inputSolicitudAgregar.reset();
      },
      error: (err) => {
        this.handlerError.handleError(err, 'Error al agregar la solicitud al Orden del Día');
      },
    });
  }

  /**
   * Descarga los anexos asociados al orden del día.
   */
  descargarAnexos(): void {
    if (!this.orden) return;
    const uuidOrden = this.orden.uuidOrdenDelDia;
    let nombreOrden = this.orden.nombre || 'Orden';

    nombreOrden = nombreOrden
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-zA-Z0-9]/g, '_');

    this.solicitudesService.descargarAnexosOrdenDelDia(uuidOrden, nombreOrden).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${nombreOrden}.zip`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.toastService.showError('Error', 'El orden del día no tiene anexos para descargar.');
      },
    });
  }

  /**
   * Exporta el orden del día en formato Word.
   */
  exportarOrdenDelDia(): void {
    if (!this.orden) return;

    const uuidOrden = this.orden.uuidOrdenDelDia;

    this.solicitudesService.exportarOrdenDelDia(uuidOrden).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);

        const nombreOrden = (this.orden.nombre || 'Orden_Del_Dia')
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')
          .replace(/[^a-zA-Z0-9]/g, '_');

        const a = document.createElement('a');
        a.href = url;
        a.download = `${nombreOrden}.docx`;
        a.click();

        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.log(err);
        this.toastService.showError('Error', 'No se pudo exportar el Orden del Día');
      },
    });
  }

  /**
   * Exporta el orden del día con respuestas del consejo en formato Word.
   */
  exportarOrdenDelDiaConRespuestas(): void {
    if (!this.orden) return;

    const uuidOrden = this.orden.uuidOrdenDelDia;

    this.solicitudesService.exportarOrdenDelDiaConRespuestas(uuidOrden).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);

        const nombreOrden = (this.orden.nombre || 'Orden_Del_Dia_Respuestas')
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')
          .replace(/[^a-zA-Z0-9]/g, '_');

        const a = document.createElement('a');
        a.href = url;
        a.download = `${nombreOrden}.docx`;
        a.click();

        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.log(err);
        this.toastService.showError('Error', 'No se pudo exportar el Orden del Día con respuestas');
      },
    });
  }

  /**
   * Exporta el orden del día en formato merge/reunión (nombre + descripción + respuestas) en Word.
   */
  exportarOrdenDelDiaMerge(): void {
    if (!this.orden) return;

    const uuidOrden = this.orden.uuidOrdenDelDia;

    this.solicitudesService.exportarOrdenDelDiaMerge(uuidOrden).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);

        const nombreOrden = (this.orden.nombre || 'Orden_Del_Dia_Reunion')
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')
          .replace(/[^a-zA-Z0-9]/g, '_');

        const a = document.createElement('a');
        a.href = url;
        a.download = `${nombreOrden}.docx`;
        a.click();

        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.log(err);
        this.toastService.showError(
          'Error',
          'No se pudo exportar el Orden del Día para la reunión'
        );
      },
    });
  }

  /**
   * Registra una solicitud dentro del orden del día.
   * @param solicitud Solicitud a registrar
   */
  registrarSolicitudesEnOrdenDelDia(solicitud: SolicitudDTORespuesta): void {
    const peticion: SolicitudActualizarDTOPeticion = {
      consecutivo: solicitud.consecutivo || '',
      nombre: solicitud.nombre,
      descripcion: solicitud.descripcion || '',
      estado: AGREGADO_EN_EL_ORDEN_DEL_DIA,
      uuidFuncionario: solicitud.objFuncionario.uuidUsuario,
      uuidOrdenDelDia: this.orden.uuidOrdenDelDia,
    };

    this.solicitudesService.actualizarSolicitud(solicitud.uuidSolicitud, peticion).subscribe({
      next: (res) => {
        this.toastService.showSuccess(
          'Éxito',
          'Solicitud registrada en Orden del Día correctamente'
        );
      },
      error: (err) => {
        this.handlerError.handleError(err, 'Error al registrar la solicitud en el Orden del Día');
      },
    });
  }

  /**
   * Cierra el componente y notifica al componente padre.
   */
  close() {
    this.visibleChange.emit(false);
  }
}
