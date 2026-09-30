import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot } from '@angular/router';
import { UsuariosService } from '../../services/usuarios-service';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

/**
 * Guard que valida el acceso a rutas según roles del usuario.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  constructor(private router: Router, private usuariosService: UsuariosService) {}

  /**
   * Verifica si el usuario posee alguno de los roles requeridos para la ruta.
   *
   * @param route información de la ruta activa
   * @returns Observable<boolean> con el resultado de la validación
   */
  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    if (typeof window !== 'undefined' && window.localStorage) {
      const uuidUsuario = localStorage.getItem('currentUser');
      const expectedRoles = route.data['roles'] as string[];

      if (uuidUsuario) {
        return this.usuariosService.getUsuario(uuidUsuario).pipe(
          map(user => {
            const userRoles: string[] = user.roles.map((r: any) => r.nombre);
            const hasRole = expectedRoles.some(role => userRoles.includes(role));
            if (hasRole) {
              return true;
            }
            this.router.navigate(['/login']);
            return false;
          }),
          catchError(() => {
            this.router.navigate(['/login']);
            return of(false);
          })
        );
      }
    }

    this.router.navigate(['/login']);
    return of(false);
  }
}
