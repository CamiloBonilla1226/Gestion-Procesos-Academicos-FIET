import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { Observable, catchError, map, of } from 'rxjs';
import { UsuariosService } from '../../services/usuarios-service';
import { rutaDeInicio } from '../../utils/rutas-por-rol';

export const inicioGuard: CanActivateFn = (): Observable<UrlTree> => {
  const router = inject(Router);
  const login = router.parseUrl('/login');
  if (typeof window === 'undefined' || !window.localStorage) return of(login);
  const token = localStorage.getItem('authToken');
  const uuidUsuario = localStorage.getItem('currentUser');
  if (!token || !uuidUsuario) return of(login);
  return inject(UsuariosService)
    .getUsuario(uuidUsuario)
    .pipe(
      map(usuario => {
        const ruta = rutaDeInicio((usuario.roles ?? []).map(r => r.nombre));
        return ruta ? router.parseUrl(ruta) : login;
      }),
      catchError(() => of(login))
    );
};
