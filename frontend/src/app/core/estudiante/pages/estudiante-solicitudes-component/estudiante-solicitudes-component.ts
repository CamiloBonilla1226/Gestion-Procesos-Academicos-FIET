import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarEstudianteComponent } from '../../components/side-bar-estudiante-component/side-bar-estudiante-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { EstSolicitudesComponent } from '../../../../shared/pages/est-solicitudes-component/est-solicitudes-component';

@Component({
  selector: 'app-estudiante-solicitudes-component',
  imports: [PageComponent],
  templateUrl: './estudiante-solicitudes-component.html'
})
export class EstudianteSolicitudesComponent {
  sidebar = SideBarEstudianteComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = EstSolicitudesComponent;
}
