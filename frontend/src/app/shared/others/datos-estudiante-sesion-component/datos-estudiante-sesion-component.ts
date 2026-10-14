import { Component, DestroyRef, Input, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../../core/services/auth-service';
import { FACULTAD_ESTUDIANTE } from '../../../core/constantes/procesos-academicos';

interface DatoSesion {
  etiqueta: string;
  valor: string;
}

interface UsuarioSesion {
  nombres?: string | null;
  apellidos?: string | null;
  tipoDocumento?: string | null;
  numeroDocumento?: string | null;
  correoElectronico?: string | null;
}

export const NOTA_DATOS_DARCA = 'Si algún dato es incorrecto, debes corregirlo ante DARCA antes de radicar la solicitud. La información aquí mostrada es la que quedará registrada en el trámite.';

@Component({
  selector: 'app-datos-estudiante-sesion-component',
  templateUrl: './datos-estudiante-sesion-component.html',
  styleUrl: './datos-estudiante-sesion-component.css'
})
export class DatosEstudianteSesionComponent implements OnInit {
  @Input() codigoEstudiantil: string | null | undefined = null;

  readonly nota = NOTA_DATOS_DARCA;

  private usuario: UsuarioSesion | null = null;
  private destroyRef = inject(DestroyRef);

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.usuario$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(usuario => (this.usuario = usuario));
  }

  get datos(): DatoSesion[] {
    const usuario = this.usuario;
    return [
      { etiqueta: 'Nombres', valor: usuario?.nombres ?? '' },
      { etiqueta: 'Apellidos', valor: usuario?.apellidos ?? '' },
      { etiqueta: 'Tipo de documento', valor: usuario?.tipoDocumento ?? '' },
      { etiqueta: 'Número de documento', valor: usuario?.numeroDocumento ?? '' },
      { etiqueta: 'Código estudiantil', valor: this.codigoEstudiantil ?? '' },
      { etiqueta: 'Correo electrónico', valor: usuario?.correoElectronico ?? '' },
      { etiqueta: 'Facultad', valor: FACULTAD_ESTUDIANTE }
    ].filter(dato => dato.valor.trim() !== '');
  }
}
