import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { FunAcadSolicitudesContentComponent } from '../content/fun-acad-solicitudes-content-component/fun-acad-solicitudes-content-component';

@Component({
  selector: 'app-fun-acad-solicitudes-component',
  imports: [ContentComponent],
  templateUrl: './fun-acad-solicitudes-component.html'
})
export class FunAcadSolicitudesComponent {
  content = FunAcadSolicitudesContentComponent;
}
