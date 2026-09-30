import { Component } from '@angular/core';
import { PageSimpleComponent } from '../../../../shared/pages/page-simple-component/page-simple-component';
import { CommonModule } from '@angular/common';
import { LoginFormComponent } from '../../../../shared/login-form-component/login-form-component';

/**
 * Componente de inicio de sesión.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-login-component',
  imports: [
    CommonModule ,
    PageSimpleComponent
  ],
  templateUrl: './login-component.html',
  styleUrl: './login-component.css'
})
export class LoginComponent {
  /**
   * Componente del formulario de inicio de sesión.
   */
  loginFormComponent = LoginFormComponent
}
