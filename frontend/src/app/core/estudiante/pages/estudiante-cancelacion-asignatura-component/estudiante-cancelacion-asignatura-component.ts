import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarEstudianteComponent } from '../../components/side-bar-estudiante-component/side-bar-estudiante-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { EstCancelacionAsignaturaComponent } from '../../../../shared/pages/est-cancelacion-asignatura-component/est-cancelacion-asignatura-component';

@Component({
  selector: 'app-estudiante-cancelacion-asignatura-component',
  imports: [PageComponent],
  templateUrl: './estudiante-cancelacion-asignatura-component.html'
})
export class EstudianteCancelacionAsignaturaComponent {
  sidebar = SideBarEstudianteComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = EstCancelacionAsignaturaComponent;
}
