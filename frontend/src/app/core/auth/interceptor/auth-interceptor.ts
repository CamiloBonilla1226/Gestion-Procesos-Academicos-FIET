import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Interceptor HTTP para agregar el token de autenticación a las peticiones.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  /**
   * Token de autenticación almacenado en el navegador.
   */
  const token = localStorage.getItem('authToken');

  /**
   * Cabeceras de la petición HTTP.
   */
  let headers = req.headers || new Headers(); 
  
  if (token)
    headers = headers.set('Authorization', `Bearer ${token}`);
  const authReq = req.clone({ headers });
  return next(authReq);
};
