import { CommonModule } from '@angular/common';
import { Component, Input, ViewChild } from '@angular/core';
import { ButtonComponent } from '../buttons/button-component/button-component';
import { GenericDialogFormComponent } from '../generic-dialog-form-component/generic-dialog-form-component';
import { InputPasswordComponent } from '../inputs/input-password-component/input-password-component';
import { UsuariosService } from '../../core/services/usuarios-service';
import { CambioContraseñaDTOPeticion } from '../../core/models/Usuario/DTORequest/CambioContraseñaDTOPeticion';
import { ToastService } from '../../core/services/toast-service';
import { ErrorHandlerService } from '../../core/services/error-handler-service';

/**
 * Componente para cambiar la contraseña de un usuario específico.
 * Permite abrir un diálogo con inputs de contraseña actual y nueva,
 * y enviar la actualización al backend.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-change-password-component',
  imports: [CommonModule, ButtonComponent, GenericDialogFormComponent, InputPasswordComponent],
  templateUrl: './change-password-component.html',
  styleUrl: './change-password-component.css'
})
export class ChangePasswordComponent {

  /** UUID del usuario al que se le actualizará la contraseña */
  @Input() uuidUsuario!: string;

  /** Controla la visibilidad del diálogo */
  visible = false;

  /** Input de contraseña actual */
  @ViewChild('inputOldPassword') inputOldPassword!: InputPasswordComponent;

  /** Input de nueva contraseña */
  @ViewChild('inputNewPassword') inputNewPassword!: InputPasswordComponent;

  constructor(
    private usuarioService: UsuariosService,
    private toastService: ToastService,
    private errorHandlerService: ErrorHandlerService
  ){}

  /** Abre el diálogo y resetea los campos de contraseña */
  open() {
    this.visible = true;
    if (this.inputOldPassword) {
      this.inputOldPassword.value = '';
      this.inputOldPassword.touched = false;
    }
    if (this.inputNewPassword) {
      this.inputNewPassword.value = '';
      this.inputNewPassword.touched = false;
    }
  }

  /** Cierra el diálogo */
  close() {
    this.visible = false;
  }

  /** Valida los inputs y envía la solicitud de cambio de contraseña */
  save() {
    this.inputOldPassword.touched = true;
    this.inputNewPassword.touched = true;

    if (this.inputOldPassword.isInvalid() || this.inputNewPassword.isInvalid()) return;

    const peticion: CambioContraseñaDTOPeticion = {
      contraseña: this.inputOldPassword.value,
      nuevaContraseña: this.inputNewPassword.value
    };

    this.usuarioService.cambiarContraseña(this.uuidUsuario, peticion).subscribe({
      next: () => {
        this.close();
        this.toastService.showSuccess("Éxito", "La contraseña ha sido actualizada con éxito.");
      },
      error: (err) => {
        this.close();
        this.errorHandlerService.handleError(err, "Error actualizando contraseña.");
      }
    });
  }
}
