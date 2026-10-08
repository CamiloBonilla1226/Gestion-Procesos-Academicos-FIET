import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { EstDetalleSolicitudContentComponent } from '../content/est-detalle-solicitud-content-component/est-detalle-solicitud-content-component';

@Component({
  selector: 'app-est-detalle-solicitud-component',
  imports: [ContentComponent],
  templateUrl: './est-detalle-solicitud-component.html'
})
export class EstDetalleSolicitudComponent {
  content = EstDetalleSolicitudContentComponent;
}
