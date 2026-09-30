import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';

/**
 * Servicio encargado de mostrar notificaciones tipo toast en la aplicación.
 * Utiliza SweetAlert2 para mostrar mensajes informativos, de éxito y de error.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Injectable({
  providedIn: 'root',
})
export class ToastService {
  /**
   * Configuración base para los toast mostrados en pantalla.
   */
  private baseToastConfig = {
    toast: true,
    position: 'top-end' as const,
    showConfirmButton: false,
    timerProgressBar: true,
    customClass: {
      popup: 'my-toast-popup',
    },
  };

  /**
   * Muestra un toast personalizado con estilos definidos.
   *
   * @param title Título del mensaje
   * @param message Contenido del mensaje
   * @param background Color o gradiente de fondo
   * @param iconColor Color del icono y título
   * @param timer Tiempo de visualización del toast
   */
  private showToast(
    title: string,
    message: string,
    background: string,
    iconColor: string,
    timer: number
  ) {
    Swal.fire({
      ...this.baseToastConfig,
      timer,
      background,
      iconColor,
      html: `
      <div style="display:flex; justify-content:flex-start; align-items:center;">
        <div style="display:flex; flex-direction:column;">
          <div style="color:${iconColor}; font-weight:bold; font-family:'Titillium Web', sans-serif; font-size:12px;">
            ${title}
          </div>
          <div style="color:#3C3B3F; font-family:'Titillium Web', sans-serif; font-size:12px;">
            ${message}
          </div>
        </div>
      </div>
    `,
    });
  }

  /**
   * Muestra un mensaje informativo al usuario.
   */
  showInfo(title: string, message: string) {
    this.showToast(
      title,
      message,
      'linear-gradient(to right, #1D72D3 5%, #E8F1FB 5%)',
      '#1D72D3',
      3000
    );
  }

  /**
   * Muestra un mensaje de éxito al usuario.
   */
  showSuccess(title: string, message: string) {
    this.showToast(
      title,
      message,
      'linear-gradient(to right, #5BAE40 5%, #EFF7EC 5%)',
      '#5BAE40',
      3000
    );
  }

  /**
   * Muestra un mensaje de error al usuario.
   */
  showError(title: string, message: string) {
    this.showToast(
      title,
      message,
      'linear-gradient(to right, #ED7D31 5%, #FDF2EA 5%)',
      '#ED7D31',
      5000
    );
  }
}
