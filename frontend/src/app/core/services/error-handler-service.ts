import { Injectable } from '@angular/core';
import { ToastService } from './toast-service';

/**
 * Servicio centralizado para el manejo de errores del sistema.
 * Se encarga de procesar errores HTTP o de ejecución y mostrar
 * mensajes amigables al usuario mediante notificaciones.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Injectable({
  providedIn: 'root'
})
export class ErrorHandlerService {

  constructor(private toastService: ToastService) { }

  /**
   * Procesa un error recibido y muestra un mensaje descriptivo al usuario.
   * Soporta errores con estructura de campos, mensajes personalizados
   * y errores genéricos.
   *
   * @param err Objeto de error capturado
   * @param titulo Título del mensaje de error
   * @param mensaje Mensaje base a mostrar
   */
  public handleError(
    err: any,
    titulo: string = 'Error',
    mensaje: string = 'Ocurrió un error'
  ): void {
    let error = '';

    console.error(`${titulo}`, err);

    if (err.error && typeof err.error === 'object' && !err.error.mensaje && !err.error.descripcion) {
      const fieldErrors = Object.entries(err.error)
        .filter(([_, value]) => typeof value === 'string')
        .map(([key, value]) => `${key}: ${value}`)
        .join(' | ');
      if (fieldErrors) 
        error = fieldErrors;
    } else if (err.error && err.error.mensaje) 
      error = err.error.mensaje;
    else if (err.error && err.error.descripcion)
      error = err.error.descripcion;
    else 
      error = err.message || 'Error desconocido';

    this.toastService.showError(titulo, `${mensaje}. ${error}`);
  }
}
