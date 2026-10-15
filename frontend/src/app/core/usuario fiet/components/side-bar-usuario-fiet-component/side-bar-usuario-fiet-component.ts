import { Component, DestroyRef, inject } from '@angular/core';
import { SidebarComponent } from '../../../../shared/sidebars/sidebar-component/sidebar-component';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../../services/auth-service';
import { ROLES_ACADEMICOS } from '../../../constantes/procesos-academicos';

const BOTONES_USUARIO_FIET = [
  { label: '| Mi Usuario', imgUrl: 'buttons/usuario.svg', route: '/usuario-fiet' },
  { label: '| Solicitudes', imgUrl: 'buttons/solicitudes.svg', route: '/usuario-fiet/solicitudes' }
];

const BOTON_NOTIFICACIONES = { label: '| Notificaciones', imgUrl: 'buttons/solicitudes.svg', route: '/docente/notificaciones' };

/**
 * Componente del sidebar para el usuario FIET.
 * Permite la navegación entre las diferentes rutas disponibles para el usuario.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component({
  selector: 'app-side-bar-usuario-fiet-component',
  imports: [SidebarComponent],
  templateUrl: './side-bar-usuario-fiet-component.html',
  styleUrl: './side-bar-usuario-fiet-component.css'
})
export class SideBarUsuarioFietComponent {
  
  botones = BOTONES_USUARIO_FIET;

  constructor(private router: Router){
    inject(AuthService).usuario$.pipe(takeUntilDestroyed(inject(DestroyRef))).subscribe(usuario => {
      const esDocente = (usuario?.roles ?? []).some(rol => rol.nombre === ROLES_ACADEMICOS.DOCENTE);
      this.botones = esDocente ? [...BOTONES_USUARIO_FIET, BOTON_NOTIFICACIONES] : BOTONES_USUARIO_FIET;
    });
  }

  /**
   * Maneja el evento de clic en una opción del sidebar.
   * Navega a la ruta recibida como parámetro.
   *
   * @param route Ruta a la que se debe navegar
   */
  onSidebarButtonClick(route: string) {
    this.router.navigate([route]);
  }
}
