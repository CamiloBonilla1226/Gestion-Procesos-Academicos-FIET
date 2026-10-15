import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { DocDetalleNotificacionContentComponent } from '../content/doc-detalle-notificacion-content-component/doc-detalle-notificacion-content-component';

@Component({
  selector: 'app-doc-detalle-notificacion-component',
  imports: [ContentComponent],
  templateUrl: './doc-detalle-notificacion-component.html'
})
export class DocDetalleNotificacionComponent {
  content = DocDetalleNotificacionContentComponent;
}
