import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { TRAMITES_ESTUDIANTE } from '../../../../core/constantes/catalogo-estudiante';
import { EnlaceFormatoComponent } from '../../../others/enlace-formato-component/enlace-formato-component';

@Component({
  selector: 'app-est-catalogo-solicitudes-content-component',
  imports: [EnlaceFormatoComponent],
  templateUrl: './est-catalogo-solicitudes-content-component.html',
  styleUrls: ['../estilos-academicos.css', './est-catalogo-solicitudes-content-component.css']
})
export class EstCatalogoSolicitudesContentComponent {
  tramites = TRAMITES_ESTUDIANTE;

  constructor(private router: Router) {}

  iniciar(evento: Event, ruta: string): void {
    evento.preventDefault();
    this.router.navigateByUrl(ruta);
  }
}
