import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

/**
 * Guard que valida el acceso a rutas protegidas.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  /**
   * Verifica si el usuario puede acceder a la ruta.
   *
   * @returns true si existe token de autenticación, false en caso contrario
   */
  canActivate(): boolean {
    if (typeof window !== 'undefined' && window.localStorage) {
      const token = localStorage.getItem('authToken');
      if (token) {
        return true;
      }
    }

    this.router.navigate(['/login']);
    return false;
  }
}
