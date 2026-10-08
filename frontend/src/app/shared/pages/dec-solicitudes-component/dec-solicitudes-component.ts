import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { DecSolicitudesContentComponent } from '../content/dec-solicitudes-content-component/dec-solicitudes-content-component';

@Component({
  selector: 'app-dec-solicitudes-component',
  imports: [ContentComponent],
  templateUrl: './dec-solicitudes-component.html'
})
export class DecSolicitudesComponent {
  content = DecSolicitudesContentComponent;
}
