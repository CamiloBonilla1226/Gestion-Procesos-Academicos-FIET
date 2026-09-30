import { Component } from '@angular/core';
import { AvatarSesionComponent } from '../../avatar/avatar-sesion-component/avatar-sesion-component';
import { AuthService } from '../../../core/services/auth-service';

/**
 * Componente principal del encabezado de la aplicación.
 * Se encarga de mostrar información del usuario, su avatar y el rol actual.
 * 
 * @autor Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-header-main-component',
  imports: [AvatarSesionComponent],
  templateUrl: './header-main-component.html',
  styleUrl: './header-main-component.css'
})
export class HeaderMainComponent {
  /**
   * Nombre completo del usuario actual.
   */
  nombre: string = '';

  /**
   * Iniciales del usuario para mostrar en el avatar.
   */
  avatarLabel: string = '';

  /**
   * Lista de roles asociados al usuario.
   */
  roles: string[] = [];

  /**
   * Rol actual del usuario.
   */
  currentRole: string = '';

  /**
   * Constructor del componente.
   * 
   * @param authService Servicio de autenticación para obtener datos del usuario.
   */
  constructor(private authService: AuthService) {}

  /**
   * Inicializa el componente y suscribirse a los cambios del usuario actual.
   * Asigna nombre, avatarLabel y currentRole según los datos del usuario.
   */
  ngOnInit(): void {
    this.authService.usuario$.subscribe(user => {
      if (user) {
        this.nombre = `${user.nombres}`;
        this.avatarLabel = this.getAvatarLabel(user.nombres, user.apellidos);
        this.currentRole = user.roles[0].nombre;
      }
    });
  }

  /**
   * Genera las iniciales del avatar a partir del nombre y apellido del usuario.
   * 
   * @param nombre Nombre del usuario.
   * @param apellido Apellido del usuario.
   * @returns Iniciales en mayúscula para mostrar en el avatar.
   */
  private getAvatarLabel(nombre: string, apellido: string): string {
    return (
      (nombre?.charAt(0) ?? '').toUpperCase() +
      (apellido?.charAt(0) ?? '').toUpperCase()
    );
  }
}
