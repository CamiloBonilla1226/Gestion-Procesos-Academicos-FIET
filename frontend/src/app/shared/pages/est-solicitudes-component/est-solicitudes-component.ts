import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { EstSolicitudesContentComponent } from '../content/est-solicitudes-content-component/est-solicitudes-content-component';

@Component({
  selector: 'app-est-solicitudes-component',
  imports: [ContentComponent],
  templateUrl: './est-solicitudes-component.html'
})
export class EstSolicitudesComponent {
  content = EstSolicitudesContentComponent;
}
