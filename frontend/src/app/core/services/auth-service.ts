import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, switchMap, catchError } from 'rxjs/operators';
import { SesionDTOPeticion } from '../models/Sesión/DTORequest/SesionDTOPeticion';
import { UsuarioTokenizadoDTORespuesta } from '../models/Sesión/DTPResponse/UsuarioTokenizadoDTORespuesta';
import { UsuarioDTORespuesta } from '../models/Usuario/DTOResponse/UsuarioDTORespuesta';
import { environment } from '../../../enviroments/environment';
import { UsuariosService } from './usuarios-service';

/**
 * Servicio de autenticación del sistema.
 * Se encarga de manejar el inicio y cierre de sesión,
 * así como la persistencia y obtención del usuario autenticado.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  /**
   * URL base para las operaciones de sesión.
   */
  private url = `${environment.apiUrl}/sesiones`;

  /**
   * Subject que mantiene el estado del usuario autenticado.
   */
  private usuarioSubject = new BehaviorSubject<UsuarioDTORespuesta | null>(null);

  /**
   * Observable público del usuario autenticado.
   */
  public usuario$ = this.usuarioSubject.asObservable();

  /**
   * Subject que indica si el usuario ya fue cargado.
   */
  private usuarioCargadoSubject = new BehaviorSubject<boolean>(false);

  /**
   * Observable público que indica si el usuario ya fue inicializado.
   */
  public usuarioCargado$ = this.usuarioCargadoSubject.asObservable();

  constructor(
    private http: HttpClient,
    private usuariosService: UsuariosService
  ) {}

  /**
   * Inicializa el usuario autenticado a partir del almacenamiento local.
   * Verifica si existe una sesión activa y carga la información del usuario.
   *
   * @returns Observable con el usuario autenticado o null
   */
  inicializarUsuario(): Observable<UsuarioDTORespuesta | null> {

    if (typeof window === 'undefined') {
      this.usuarioCargadoSubject.next(true);
      return of(null);
    }

    const uuid = localStorage.getItem('currentUser');

    if (!uuid) {
      this.usuarioSubject.next(null);
      this.usuarioCargadoSubject.next(true);
      return of(null);
    }

    return this.usuariosService.getUsuario(uuid).pipe(
      tap(usuario => {
        this.usuarioSubject.next(usuario);
        this.usuarioCargadoSubject.next(true);
      }),
      catchError(() => {
        this.logout();
        this.usuarioCargadoSubject.next(true);
        return of(null);
      })
    );
  }

  /**
   * Realiza el inicio de sesión del usuario.
   * Guarda el token y el identificador del usuario en el almacenamiento local.
   *
   * @param credentials Credenciales de inicio de sesión
   * @returns Observable con la información del usuario autenticado
   */
  login(credentials: SesionDTOPeticion): Observable<UsuarioDTORespuesta> {
    return this.http.post<UsuarioTokenizadoDTORespuesta>(this.url, credentials).pipe(
      tap(tokenizado => {
        localStorage.setItem('currentUser', tokenizado.uuidUsuario);
        localStorage.setItem('authToken', tokenizado.token);
      }),
      switchMap(tokenizado =>
        this.usuariosService.getUsuario(tokenizado.uuidUsuario)
      ),
      tap(usuario => this.usuarioSubject.next(usuario))
    );
  }

  /**
   * Cierra la sesión del usuario actual.
   * Elimina la información almacenada y limpia el estado del usuario.
   */
  logout(): void {
    this.usuarioSubject.next(null);
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authToken');
  }

  /**
   * Obtiene el usuario autenticado actualmente.
   *
   * @returns Usuario autenticado o null
   */
  getUsuarioActual(): UsuarioDTORespuesta | null {
    return this.usuarioSubject.getValue();
  }
}
