import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../../core/services/auth-service';
import { FACULTAD_ESTUDIANTE } from '../../../core/constantes/procesos-academicos';

interface DatoSesion {
  etiqueta: string;
  valor: string;
}

export const NOTA_DATOS_DARCA = 'Si algún dato es incorrecto, debes corregirlo ante DARCA antes de radicar la solicitud. La información aquí mostrada es la que quedará registrada en el trámite.';

@Component({
  selector: 'app-datos-estudiante-sesion-component',
  templateUrl: './datos-estudiante-sesion-component.html',
  styleUrl: './datos-estudiante-sesion-component.css'
})
export class DatosEstudianteSesionComponent implements OnInit {
  readonly nota = NOTA_DATOS_DARCA;

  datos: DatoSesion[] = [];

  private destroyRef = inject(DestroyRef);

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.usuario$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(usuario => {
      this.datos = [
        { etiqueta: 'Nombres', valor: usuario?.nombres ?? '' },
        { etiqueta: 'Apellidos', valor: usuario?.apellidos ?? '' },
        { etiqueta: 'Tipo de documento', valor: usuario?.tipoDocumento ?? '' },
        { etiqueta: 'Número de documento', valor: usuario?.numeroDocumento ?? '' },
        { etiqueta: 'Correo electrónico', valor: usuario?.correoElectronico ?? '' },
        { etiqueta: 'Facultad', valor: FACULTAD_ESTUDIANTE }
      ].filter(dato => dato.valor.trim() !== '');
    });
  }
}
