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

/**
 * Componente de formulario de login.
 * Permite ingresar usuario y contraseña, autenticar al usuario y redirigir según rol.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-login-form-component',
  imports: [CommonModule, InputTextComponent, InputPasswordComponent, SimpleButtonComponent],
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

    this.authService.login(request).subscribe({
      next: (userInfo) => {
        const roles = userInfo.roles.map(r => r.nombre);
        if (roles.includes('Secretario General')) 
          this.router.navigate(['/sec-general']);
        else if (roles.includes('Funcionario'))
          this.router.navigate(['/funcionario']);
        else if(roles.includes('Secretaria Decanatura FIET'))
          this.router.navigate(['/sec-fiet']);
        else if (roles.some(r => this.usuariosFiet.includes(r))) 
          this.router.navigate(['/usuario-fiet']);
        else
          this.toastService.showError('Error ingresando al sistema', 'El usuario no tiene un rol con vistas definidas en el sistema.');
      },
      error: (err) => this.errorHandlerService.handleError(err, "Error en Autenticación")
    });
  }
}
