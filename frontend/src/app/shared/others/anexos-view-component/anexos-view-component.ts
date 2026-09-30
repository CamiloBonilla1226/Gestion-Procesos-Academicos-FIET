import { Component, Input } from '@angular/core';
import { AnexoDTORespuesta } from '../../../core/models/Solicitudes/DTOResponse/AnexoDTORespuesta';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';

/**
 * Componente para visualizar y descargar anexos.
 * Permite mostrar una lista de anexos y descargar cada uno mediante un enlace.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
import { environment } from '../../../../enviroments/environment';
import { ErrorHandlerService } from '../../../core/services/error-handler-service';

@Component({
  selector: 'app-anexos-view-component',
  imports: [CommonModule],
  templateUrl: './anexos-view-component.html',
  styleUrls: ['./anexos-view-component.css']
})
export class AnexosViewComponent {
  /**
   * Lista de anexos que se mostrarán en el componente.
   */
  @Input() anexos: AnexoDTORespuesta[] = [];

  constructor(
    private http: HttpClient,
    private errorHandlerService:ErrorHandlerService
  ) {}

  /**
   * Descarga el anexo seleccionado.
   * Crea un enlace temporal para descargar el archivo con el nombre correcto.
   * 
   * @param anexo Anexo a descargar.
   */
  descargar(anexo: AnexoDTORespuesta) {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.getToken()}`
    });

    const url = `${environment.domain}${anexo.urlAnexo.replace(/^\/+/, '')}`;
    this.http.get(url, { responseType: 'blob', headers }).subscribe({
      next: (blob) => {
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = decodeURIComponent(anexo.nombre + this.getFileExtension(anexo.urlAnexo));
        link.click();
        window.URL.revokeObjectURL(link.href);
      },
      error: (err) => {
        this.errorHandlerService.handleError(err, 'Error', "Error obteniendo el recurso");
      }
    });
  }

  /**
   * Obtiene el token de autenticación almacenado en localStorage.
   * 
   * @returns Token como string o vacío si no existe.
   */
  private getToken(): string {
    return localStorage.getItem('token') || '';
  }

  /**
   * Obtiene la extensión del archivo a partir de la URL.
   * 
   * @param url URL del archivo.
   * @returns Extensión del archivo incluyendo el punto inicial.
   */
  private getFileExtension(url: string): string {
    return url.substring(url.lastIndexOf('.'));
  }
}
