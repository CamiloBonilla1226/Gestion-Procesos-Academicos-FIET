import { Component } from '@angular/core';
import { ContentComponent } from '../content/content-component/content-component';
import { AdminAcademicaContentComponent } from '../content/admin-academica-content-component/admin-academica-content-component';

@Component({
  selector: 'app-admin-academica-component',
  imports: [ContentComponent],
  templateUrl: './admin-academica-component.html'
})
export class AdminAcademicaComponent {
  content = AdminAcademicaContentComponent;
}
