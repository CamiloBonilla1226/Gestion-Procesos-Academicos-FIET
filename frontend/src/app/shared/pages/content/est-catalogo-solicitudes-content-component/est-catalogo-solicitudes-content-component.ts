import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { TRAMITES_ESTUDIANTE } from '../../../../core/constantes/catalogo-estudiante';

@Component({
  selector: 'app-est-catalogo-solicitudes-content-component',
  templateUrl: './est-catalogo-solicitudes-content-component.html',
  styleUrl: './est-catalogo-solicitudes-content-component.css'
})
export class EstCatalogoSolicitudesContentComponent {
  tramites = TRAMITES_ESTUDIANTE;

  constructor(private router: Router) {}

  iniciar(evento: Event, ruta: string): void {
    evento.preventDefault();
    this.router.navigateByUrl(ruta);
  }
}
