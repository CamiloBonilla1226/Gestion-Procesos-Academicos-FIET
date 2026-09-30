import { Component, Input, Output, EventEmitter, OnInit, TemplateRef, ViewChild , ViewChildren, QueryList} from '@angular/core';
import { CommonModule } from '@angular/common';
import { GenericDialogStepsFormComponent } from '../../../../shared/generic-dialog-steps-form-component/generic-dialog-steps-form-component';
import { InputTextComponent } from '../../../../shared/inputs/input-text-component/input-text-component';
import { InputTextTareaComponent } from '../../../../shared/inputs/input-text-tarea-component/input-text-tarea-component';
import { InputSelectComponent } from '../../../../shared/inputs/input-select-component/input-select-component';
import { InputAnexoUploadComponent } from '../../../../shared/inputs/input-anexo-upload-component/input-anexo-upload-component';
import { TipoSolicitudService } from '../../../services/tipo-solicitud-service';
import { SolicitudesService } from '../../../services/solicitudes-service';
import { ToastService } from '../../../services/toast-service';
import { ErrorHandlerService } from '../../../services/error-handler-service';
import { TipoSolicitudDTORespuesta } from '../../../models/TipoSolicitud/DTOResponse/TipoSolicitudDTORespuesta';
import { SolicitudPublicaDTOPeticion } from '../../../models/Solicitudes/DTORequest/SolicitudPublicaDTOPeticion';
import { TIPOS_DOCUMENTO } from '../../../constantes/constantes';

/**
 * Componente encargado de permitir a un usuario público
 * diligenciar y enviar una solicitud mediante un formulario
 * dividido en varios pasos.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-enviar-solicitud-usuario-publico-component',
  standalone: true,
  imports: [
    CommonModule,
    GenericDialogStepsFormComponent,
    InputTextComponent,
    InputTextTareaComponent,
    InputSelectComponent,
    InputAnexoUploadComponent
  ],
  templateUrl: './enviar-solicitud-usuario-publico-component.html',
  styleUrl: './enviar-solicitud-usuario-publico-component.css'
})
export class EnviarSolicitudUsuarioPublicoComponent implements OnInit {

  /**
   * Controla la visibilidad del diálogo.
   */
  @Input() visible = false;

  /**
   * Emite el cambio de visibilidad del componente.
   */
  @Output() visibleChange = new EventEmitter<boolean>();

  /**
   * Modelo que almacena la información completa
   * de la solicitud pública a enviar.
   */
  solicitudPublica: SolicitudPublicaDTOPeticion = {
    nombre: '',
    descripcion: '',
    uuidTipoSolicitud: '',
    anexos: [],
    informacionSolicitante: {
      tipoDocumento: '',
      numeroDocumento: '',
      nombres: '',
      apellidos: '',
      correoElectronico: '',
      telefono: ''
    }
  };

  /**
   * Configuración de los pasos del formulario.
   */
  steps: {
    title: string;
    contentTemplate: TemplateRef<any>;
    canContinue?: () => boolean;
  }[] = [];

  @ViewChild('step1', { static: true }) step1Template!: TemplateRef<any>;
  @ViewChild('step2', { static: true }) step2Template!: TemplateRef<any>;
  @ViewChild('step3', { static: true }) step3Template!: TemplateRef<any>;

  /** Controles del paso 1 */
  @ViewChild('inputTipoDocumento') inputTipoDocumento!: InputSelectComponent;
  @ViewChild('inputNumeroDocumento') inputNumeroDocumento!: InputTextComponent;
  @ViewChild('inputNombres') inputNombres!: InputTextComponent;
  @ViewChild('inputApellidos') inputApellidos!: InputTextComponent;
  @ViewChild('inputCorreo') inputCorreo!: InputTextComponent;
  @ViewChild('inputTelefono') inputTelefono!: InputTextComponent;

  /** Controles del paso 2 */
  @ViewChild('inputNombreSolicitud') inputNombreSolicitud!: InputTextComponent;
  @ViewChild('inputDescripcionSolicitud') inputDescripcionSolicitud!: InputTextTareaComponent;
  @ViewChild('inputTipoSolicitud') inputTipoSolicitud!: InputSelectComponent;

  /** Controles del paso 3 (anexos) */
  @ViewChildren('inputAnexo') inputAnexos!: QueryList<InputAnexoUploadComponent>;

  /**
   * Opciones para el selector de tipos de solicitud.
   */
  tiposSolicitudOptions: { label: string; value: string }[] = [];

  /**
   * Lista completa de tipos de solicitud disponibles.
   */
  tiposSolicitud: TipoSolicitudDTORespuesta[] = [];

  /**
   * Tipo de solicitud seleccionado actualmente.
   */
  tipoSolicitudSeleccionado: TipoSolicitudDTORespuesta | null = null;

  /**
   * Archivos anexos cargados por el usuario.
   */
  archivosAnexos: File[] = [];

  /**
   * Información de los anexos asociados a la solicitud.
   */
  anexosSolicitud: { nombre: string }[] = [];

  /**
   * Tipos de documento disponibles.
   */
  tiposDocumento = TIPOS_DOCUMENTO;

  constructor(
    private tipoSolicitudService: TipoSolicitudService,
    private solicitudesService: SolicitudesService,
    private toastService: ToastService,
    private errorHandlerService: ErrorHandlerService
  ) {}

  /**
   * Inicializa el componente, configura los pasos
   * del formulario y carga los tipos de solicitud.
   */
  ngOnInit(): void {
    this.steps = [
      {
        title: 'Paso 1: Información del Solicitante',
        contentTemplate: this.step1Template,
        canContinue: () => this.canContinueStep1()
      },
      {
        title: 'Paso 2: Información de la Solicitud',
        contentTemplate: this.step2Template,
        canContinue: () => this.canContinueStep2()
      },
      {
        title: 'Paso 3: Tipos de Anexos',
        contentTemplate: this.step3Template
      }
    ];

    this.cargarTiposSolicitudPublicos();
  }

  /**
   * Valida si se puede continuar desde el paso 1.
   * @returns true si todos los campos obligatorios están completos
   */
  canContinueStep1(): boolean {
    const i = this.solicitudPublica.informacionSolicitante;
    return !!(
      i.tipoDocumento &&
      i.numeroDocumento &&
      i.nombres &&
      i.apellidos &&
      i.correoElectronico &&
      i.telefono
    );
  }

  /**
   * Valida si se puede continuar desde el paso 2.
   * @returns true si la información mínima de la solicitud es válida
   */
  canContinueStep2(): boolean {
    return !!(
      this.solicitudPublica.nombre?.trim() &&
      this.solicitudPublica.uuidTipoSolicitud
    );
  }

  /**
   * Obtiene los tipos de solicitud disponibles
   * para el perfil de solicitante público.
   */
  cargarTiposSolicitudPublicos(): void {
    this.tipoSolicitudService.getTiposSolicitudPorPerfil('Solicitante Publico')
      .subscribe(res => {
        this.tiposSolicitud = res;
        this.tiposSolicitudOptions = res.map(t => ({
          label: t.nombre,
          value: t.uuidTipoSolicitud
        }));
      });
  }

  /**
   * Maneja el cambio del tipo de solicitud seleccionado.
   * @param uuid identificador del tipo de solicitud
   */
  onTipoSolicitudChange(uuid: string): void {
    this.tipoSolicitudSeleccionado =
      this.tiposSolicitud.find(t => t.uuidTipoSolicitud === uuid) || null;
  }

  /**
   * Registra un archivo anexo cargado.
   * @param file archivo cargado
   * @param index posición del anexo
   * @param nombre nombre del tipo de anexo
   */
  onArchivoCargado(file: File, index: number, nombre: string): void {
    this.archivosAnexos[index] = file;
    this.anexosSolicitud[index] = { nombre };
  }

  /**
   * Elimina un archivo anexo.
   * @param index índice del anexo a remover
   */
  onArchivoRemovido(index: number): void {
    this.archivosAnexos.splice(index, 1);
    this.anexosSolicitud.splice(index, 1);
  }

  /**
   * Envía la solicitud pública junto con sus anexos.
   * Realiza validaciones de obligatoriedad antes del envío.
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

    this.solicitudPublica.anexos = [];
    const archivosOrdenados: File[] = [];

    this.archivosAnexos.forEach((file, index) => {
      if (file) {
        this.solicitudPublica.anexos.push(this.anexosSolicitud[index]);
        archivosOrdenados.push(file);
      }
    });

    this.solicitudesService
      .enviarSolicitudPublica(this.solicitudPublica, archivosOrdenados)
      .subscribe({
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
   * Restaura el formulario a su estado inicial
   * y reinicia todos los controles.
   */
  private resetFormulario(): void {
    this.solicitudPublica = {
      nombre: '',
      descripcion: '',
      uuidTipoSolicitud: '',
      anexos: [],
      informacionSolicitante: {
        tipoDocumento: '',
        numeroDocumento: '',
        nombres: '',
        apellidos: '',
        correoElectronico: '',
        telefono: ''
      }
    };

    this.archivosAnexos = [];
    this.anexosSolicitud = [];
    this.tipoSolicitudSeleccionado = null;

    this.inputTipoDocumento?.reset();
    this.inputNumeroDocumento?.reset();
    this.inputNombres?.reset();
    this.inputApellidos?.reset();
    this.inputCorreo?.reset();
    this.inputTelefono?.reset();
    this.inputNombreSolicitud?.reset();
    this.inputDescripcionSolicitud?.reset();
    this.inputTipoSolicitud?.reset();
    this.inputAnexos?.forEach(anexo => anexo.reset?.());
  }
}
