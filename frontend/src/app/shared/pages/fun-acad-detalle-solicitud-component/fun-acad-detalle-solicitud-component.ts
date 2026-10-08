import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { FunAcadDetalleSolicitudContentComponent } from '../content/fun-acad-detalle-solicitud-content-component/fun-acad-detalle-solicitud-content-component';

@Component({
  selector: 'app-fun-acad-detalle-solicitud-component',
  imports: [ContentComponent],
  templateUrl: './fun-acad-detalle-solicitud-component.html'
})
export class FunAcadDetalleSolicitudComponent {
  content = FunAcadDetalleSolicitudContentComponent;
}
