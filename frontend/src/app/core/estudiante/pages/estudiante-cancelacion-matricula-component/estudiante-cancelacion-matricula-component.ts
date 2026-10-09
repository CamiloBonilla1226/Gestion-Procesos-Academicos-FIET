import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarEstudianteComponent } from '../../components/side-bar-estudiante-component/side-bar-estudiante-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { EstCancelacionMatriculaComponent } from '../../../../shared/pages/est-cancelacion-matricula-component/est-cancelacion-matricula-component';

@Component({
  selector: 'app-estudiante-cancelacion-matricula-component',
  imports: [PageComponent],
  templateUrl: './estudiante-cancelacion-matricula-component.html'
})
export class EstudianteCancelacionMatriculaComponent {
  sidebar = SideBarEstudianteComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = EstCancelacionMatriculaComponent;
}
