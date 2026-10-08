import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { DecDetalleSolicitudContentComponent } from '../content/dec-detalle-solicitud-content-component/dec-detalle-solicitud-content-component';

@Component({
  selector: 'app-dec-detalle-solicitud-component',
  imports: [ContentComponent],
  templateUrl: './dec-detalle-solicitud-component.html'
})
export class DecDetalleSolicitudComponent {
  content = DecDetalleSolicitudContentComponent;
}
