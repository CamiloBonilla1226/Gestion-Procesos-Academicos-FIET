import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { AvatarModule } from 'primeng/avatar';
import { AvatarGroupModule } from 'primeng/avatargroup';
import { AuthService } from '../../../core/services/auth-service';
import { Router } from '@angular/router';

/**
 * Componente encargado de mostrar el avatar de sesión del usuario autenticado,
 * junto con su nombre y roles, y permitir el cierre de sesión.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-avatar-sesion-component',
  imports: [AvatarModule, AvatarGroupModule, CommonModule],
  templateUrl: './avatar-sesion-component.html',
  styleUrl: './avatar-sesion-component.css'
})
export class AvatarSesionComponent {

  /**
   * Inicial del nombre o etiqueta que se muestra en el avatar.
   */
  @Input() avatarLabel: string = '';

  /**
   * Nombre completo del usuario autenticado.
   */
  @Input() nombre: string = '';

  /**
   * Lista de roles asociados al usuario.
   */
  @Input() roles: string[] = [];

  /**
   * Rol actualmente activo del usuario.
   */
  @Input() currentRole: string = '';

  /**
   * Constructor del componente.
   *
   * @param authService Servicio de autenticación para gestionar el cierre de sesión
   * @param router Servicio de enrutamiento para redirigir al login
   */
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /**
   * Cierra la sesión del usuario actual y redirige a la pantalla de login.
   */
  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
