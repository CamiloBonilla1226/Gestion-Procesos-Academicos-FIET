import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../enviroments/environment';
import { ErrorHandlerService } from '../../core/services/error-handler-service';

/**
 * Componente para descargar y eliminar archivos de respuesta.
 * Permite descargar un archivo desde la URL proporcionada y emitir eventos al eliminarlo.
 * 
 * @author Julian David Camacho Erazo
 * {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-descargar-respuesta-component',
  imports: [CommonModule],
  templateUrl: './descargar-respuesta-component.html',
  styleUrl: './descargar-respuesta-component.css'
})
export class DescargarRespuestaComponent {

  /** UUID de la respuesta */
  @Input() uuidRespuesta!: string;

  /** URL del archivo de respuesta */
  @Input() urlRespuesta!: string;

  /** Nombre del archivo que se descargará */
  @Input() nombreArchivo!: string;

  /** Indica si se debe mostrar el botón de eliminar */
  @Input() mostrarEliminar: boolean = true;

  /** Evento que se emite cuando se elimina la respuesta */
  @Output() eliminado = new EventEmitter<void>();

  /**
   * Constructor del componente.
   * @param http Cliente HTTP para descargar archivos
   */
  constructor(
    private http: HttpClient,
    private errorHandleService: ErrorHandlerService
  ) {}

  /**
   * Descarga el archivo de respuesta desde la URL proporcionada.
   * Crea un enlace temporal para descargar el archivo.
   */
  descargar(): void {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${this.getToken()}`
    });

    const url = `${environment.domain}${this.urlRespuesta.replace(/^\/+/, '')}`;

    this.http.get(url, { responseType: 'blob', headers }).subscribe({
      next: blob => {
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = this.nombreArchivo;
        link.click();
        window.URL.revokeObjectURL(link.href);
      },
      error: err => {
        this.errorHandleService.handleError(err, 'Error', 'Error, obteniendo el recurso')
      }
    });
  }

  /**
   * Emite el evento de eliminación de la respuesta.
   */
  eliminar(): void {
    this.eliminado.emit();
  }

  /**
   * Obtiene el token de autenticación almacenado en localStorage.
   * @returns Token de autenticación como string
   */
  private getToken(): string {
    return localStorage.getItem('token') || '';
  }
}
