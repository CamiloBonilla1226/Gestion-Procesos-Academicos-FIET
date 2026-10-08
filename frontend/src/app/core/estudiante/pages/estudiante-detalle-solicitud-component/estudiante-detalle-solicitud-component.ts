import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarEstudianteComponent } from '../../components/side-bar-estudiante-component/side-bar-estudiante-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { EstDetalleSolicitudComponent } from '../../../../shared/pages/est-detalle-solicitud-component/est-detalle-solicitud-component';

@Component({
  selector: 'app-estudiante-detalle-solicitud-component',
  imports: [PageComponent],
  templateUrl: './estudiante-detalle-solicitud-component.html'
})
export class EstudianteDetalleSolicitudComponent {
  sidebar = SideBarEstudianteComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = EstDetalleSolicitudComponent;
}
