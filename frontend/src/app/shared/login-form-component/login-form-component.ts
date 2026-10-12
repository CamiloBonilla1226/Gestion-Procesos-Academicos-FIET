import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InputTextComponent } from '../inputs/input-text-component/input-text-component';
import { InputPasswordComponent } from '../inputs/input-password-component/input-password-component';
import { SimpleButtonComponent } from '../buttons/simple-button-component/simple-button-component';
import { AuthService } from '../../core/services/auth-service';
import { SesionDTOPeticion } from '../../core/models/Sesión/DTORequest/SesionDTOPeticion';
import { Router } from '@angular/router';
import { ErrorHandlerService } from '../../core/services/error-handler-service';
import { ROLES_FIET } from '../../core/constantes/constantes';
import { ToastService } from '../../core/services/toast-service';
import { finalize } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { rutaDeInicio } from '../../core/utils/rutas-por-rol';

/**
 * Componente de formulario de login.
 * Permite ingresar usuario y contraseña, autenticar al usuario y redirigir según rol.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-login-form-component',
  imports: [CommonModule, FormsModule, InputTextComponent, InputPasswordComponent, SimpleButtonComponent],
  templateUrl: './login-form-component.html',
  styleUrl: './login-form-component.css'
})
export class LoginFormComponent {
  /**
   * Nombre de usuario ingresado.
   */
  username = '';

  /**
   * Contraseña ingresada.
   */
  password = '';

  /**
   * Lista de roles que pertenecen a usuarios FIET para redirección.
   */
  usuariosFiet: string[] = ROLES_FIET; 

  enviando = false;

  /**
   * Constructor del componente.
   * 
   * @param authService Servicio de autenticación para login de usuarios.
   * @param errorHandlerService Servicio para manejar errores.
   * @param router Router para navegación según rol.
   */
  constructor(
    private authService: AuthService,
    private errorHandlerService: ErrorHandlerService,
    private router: Router,
    private toastService: ToastService 
  ) {}

  /**
   * Envía el formulario de login.
   * Realiza la autenticación usando `AuthService` y redirige según los roles del usuario.
   */
  onSubmit(): void {
    const request: SesionDTOPeticion = { username: this.username, password: this.password };

    if (this.enviando) return;
    this.enviando = true;
    this.authService.login(request).pipe(finalize(() => (this.enviando = false))).subscribe({
      next: (userInfo) => {
        const ruta = rutaDeInicio(userInfo.roles.map(r => r.nombre));
        if (ruta)
          this.router.navigate([ruta]);
        else
          this.toastService.showError('Error ingresando al sistema', 'El usuario no tiene un rol con vistas definidas en el sistema.');
      },
      error: (err) => this.errorHandlerService.handleError(err, "Error en Autenticación")
    });
  }
}
