import { Component, Input, Output, EventEmitter, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GenericDialogStepsFormComponent } from '../../../../shared/generic-dialog-steps-form-component/generic-dialog-steps-form-component';
import { InputTextComponent } from '../../../../shared/inputs/input-text-component/input-text-component';
import { InputTextTareaComponent } from '../../../../shared/inputs/input-text-tarea-component/input-text-tarea-component';
import { InputSelectComponent } from '../../../../shared/inputs/input-select-component/input-select-component';
import { SolicitudDTOPeticion } from '../../../models/Solicitudes/DTORequest/SolicitudDTOPeticion';
import { TipoSolicitudService } from '../../../services/tipo-solicitud-service';
import { SolicitudesService } from '../../../services/solicitudes-service';
import { AuthService } from '../../../services/auth-service';
import { TipoSolicitudDTORespuesta } from '../../../models/TipoSolicitud/DTOResponse/TipoSolicitudDTORespuesta';
import { InputAnexoUploadComponent } from '../../../../shared/inputs/input-anexo-upload-component/input-anexo-upload-component';
import { ToastService } from '../../../services/toast-service';
import { ErrorHandlerService } from '../../../services/error-handler-service';

/**
 * Componente encargado de permitir a los usuarios FIET
 * enviar solicitudes con información básica y anexos requeridos.
 * Implementa un formulario por pasos.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-enviar-solicitud-usuario-fiet-component',
  standalone: true,
  imports: [
    CommonModule,
    GenericDialogStepsFormComponent,
    InputTextComponent,
    InputTextTareaComponent,
    InputSelectComponent,
    InputAnexoUploadComponent
  ],
  templateUrl: './enviar-solicitud-usuario-fiet-component.html',
  styleUrl: './enviar-solicitud-usuario-fiet-component.css'
})
export class EnviarSolicitudUsuarioFietComponent implements OnInit {

  /**
   * Controla la visibilidad del diálogo.
   */
  @Input() visible = false;

  /**
   * Emite cambios en la visibilidad del diálogo.
   */
  @Output() visibleChange = new EventEmitter<boolean>();

  /**
   * Modelo que almacena la información de la nueva solicitud.
   */
  nuevaSolicitud: SolicitudDTOPeticion = {
    consecutivo: '',
    nombre: '',
    descripcion: '',
    uuidTipoSolicitud: '',
    anexos: [],
    uuidOrdenDelDia: ''
  };

  /**
   * Definición de los pasos del formulario.
   */
  steps: {
    title: string;
    contentTemplate: TemplateRef<any>;
    canContinue?: () => boolean;
  }[] = [];

  /**
   * Plantilla del primer paso.
   */
  @ViewChild('step1', { static: true }) step1Template!: TemplateRef<any>;

  /**
   * Plantilla del segundo paso.
   */
  @ViewChild('step2', { static: true }) step2Template!: TemplateRef<any>;

  /**
   * Opciones de tipos de solicitud para el selector.
   */
  tiposSolicitudOptions: { label: string; value: string }[] = [];

  /**
   * Opciones de órdenes del día para el selector.
   */
  ordenesDelDiaOptions: { label: string; value: string }[] = [];

  /**
   * Tipo de solicitud actualmente seleccionado.
   */
  tipoSolicitudSeleccionado: TipoSolicitudDTORespuesta | null = null;

  /**
   * Lista de tipos de solicitud disponibles.
   */
  tiposSolicitud: TipoSolicitudDTORespuesta[] = [];

  /**
   * Información del usuario autenticado.
   */
  usuario: any;

  /**
   * Archivos anexos cargados por el usuario.
   */
  archivosAnexos: File[] = [];

  /**
   * Metadatos de los anexos de la solicitud.
   */
  anexosSolicitud: { nombre: string }[] = [];

  /**
   * Mapa de archivos asociados a cada tipo de anexo.
   */
  archivosPorTipo: Record<string, File> = {};

  constructor(
    private tipoSolicitudService: TipoSolicitudService,
    private solicitudesService: SolicitudesService,
    private authService: AuthService,
    private toastService: ToastService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  /**
   * Inicializa los pasos del formulario y carga
   * los datos necesarios según el usuario autenticado.
   */
  ngOnInit(): void {
    this.steps = [
      {
        title: 'Paso 1: Información de la Solicitud',
        contentTemplate: this.step1Template,
        canContinue: () => this.canContinueStep1()
      },
      {
        title: 'Paso 2: Tipos de Anexos',
        contentTemplate: this.step2Template
      }
    ];

    this.authService.usuario$.subscribe(user => {
      this.usuario = user;
      if (user) {
        this.cargarTiposSolicitud(user.roles[0].nombre);
        this.cargarOrdenesDelDia();
      }
    });
  }

  /**
   * Valida si se puede continuar al segundo paso del formulario.
   */
  canContinueStep1(): boolean {
    return !!(
      this.nuevaSolicitud.nombre?.trim() &&
      this.nuevaSolicitud.uuidTipoSolicitud &&
      this.nuevaSolicitud.uuidOrdenDelDia
    );
  }

  /**
   * Carga los tipos de solicitud disponibles según el perfil del usuario.
   *
   * @param perfil Perfil del usuario
   */
  cargarTiposSolicitud(perfil: string): void {
    this.tipoSolicitudService.getTiposSolicitudPorPerfil(perfil).subscribe(res => {
      this.tiposSolicitud = res;

      this.tiposSolicitudOptions = res.map(t => ({
        label: t.nombre,
        value: t.uuidTipoSolicitud
      }));
    });
  }

  /**
   * Maneja el cambio del tipo de solicitud seleccionado.
   *
   * @param uuid Identificador del tipo de solicitud
   */
  onTipoSolicitudChange(uuid: string): void {
    this.tipoSolicitudSeleccionado =
      this.tiposSolicitud.find(t => t.uuidTipoSolicitud === uuid) || null;
  }

  /**
   * Carga las órdenes del día activas.
   */
  cargarOrdenesDelDia(): void {
    this.solicitudesService.getOrdenesDelDiaPorEstado(true).subscribe(res => {
      this.ordenesDelDiaOptions = res.map(o => ({
        label: o.nombre,
        value: o.uuidOrdenDelDia
      }));
    });
  }

  /**
   * Envía la solicitud junto con sus anexos al backend.
   */
  enviarSolicitud(): void {
    if (!this.tipoSolicitudSeleccionado) return;

    for (let i = 0; i < this.tipoSolicitudSeleccionado.anexos.length; i++) {
      const tipoAnexo = this.tipoSolicitudSeleccionado.anexos[i];

      if (tipoAnexo.obligatoriedad && !this.archivosAnexos[i]) {
        this.toastService.showError('Error', `El anexo "${tipoAnexo.nombre}" es obligatorio`);
        return;
      }
    }

    this.nuevaSolicitud.anexos = [];
    const archivosOrdenados: File[] = [];

    this.archivosAnexos.forEach((file, index) => {
      if (file) {
        this.nuevaSolicitud.anexos.push(this.anexosSolicitud[index]);
        archivosOrdenados.push(file);
      }
    });

    this.solicitudesService.enviarSolicitud(this.nuevaSolicitud, archivosOrdenados).subscribe({
      next: () => {
        this.toastService.showSuccess('Éxito', 'Solicitud enviada correctamente');
        this.cerrar();
      },
      error: err => {
        this.errorHandlerService.handleError(err, 'Error enviando la solicitud');
      }
    });
  }

  /**
   * Cierra el diálogo y limpia el formulario.
   */
  cerrar(): void {
    this.visible = false;
    this.visibleChange.emit(false);
    this.resetFormulario();
  }

  /**
   * Restablece los valores del formulario.
   */
  private resetFormulario(): void {
    this.nuevaSolicitud = {
      consecutivo: '',
      nombre: '',
      descripcion: '',
      uuidTipoSolicitud: '',
      anexos: [],
      uuidOrdenDelDia: ''
    };
    this.archivosAnexos = [];
    this.anexosSolicitud = [];
    this.archivosPorTipo = {};
    this.tipoSolicitudSeleccionado = null;
  }

  /**
   * Maneja la carga de un archivo anexo.
   *
   * @param file Archivo cargado
   * @param index Índice del anexo
   * @param nombre Nombre del anexo
   */
  onArchivoCargado(file: File, index: number, nombre: string): void {
    this.archivosAnexos[index] = file;
    this.anexosSolicitud[index] = { nombre };
  }

  /**
   * Elimina un archivo anexo cargado.
   *
   * @param index Índice del anexo
   */
  onArchivoRemovido(index: number): void {
    this.archivosAnexos.splice(index, 1);
    this.anexosSolicitud.splice(index, 1);
  }

}
