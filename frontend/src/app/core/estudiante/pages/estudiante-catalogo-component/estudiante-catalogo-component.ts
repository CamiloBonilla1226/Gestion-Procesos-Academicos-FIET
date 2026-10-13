import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarEstudianteComponent } from '../../components/side-bar-estudiante-component/side-bar-estudiante-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { EstCatalogoSolicitudesComponent } from '../../../../shared/pages/est-catalogo-solicitudes-component/est-catalogo-solicitudes-component';

@Component({
  selector: 'app-estudiante-catalogo-component',
  imports: [PageComponent],
  templateUrl: './estudiante-catalogo-component.html'
})
export class EstudianteCatalogoComponent {
  sidebar = SideBarEstudianteComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = EstCatalogoSolicitudesComponent;
}
