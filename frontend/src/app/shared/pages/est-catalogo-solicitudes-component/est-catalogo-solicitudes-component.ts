import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { EstCatalogoSolicitudesContentComponent } from '../content/est-catalogo-solicitudes-content-component/est-catalogo-solicitudes-content-component';

@Component({
  selector: 'app-est-catalogo-solicitudes-component',
  imports: [ContentComponent],
  templateUrl: './est-catalogo-solicitudes-component.html'
})
export class EstCatalogoSolicitudesComponent {
  content = EstCatalogoSolicitudesContentComponent;
}
