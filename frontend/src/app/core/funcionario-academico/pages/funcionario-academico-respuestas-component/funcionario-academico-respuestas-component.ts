import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarFuncionarioAcademicoComponent } from '../../components/side-bar-funcionario-academico-component/side-bar-funcionario-academico-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { FunAcadSolicitudesComponent } from '../../../../shared/pages/fun-acad-solicitudes-component/fun-acad-solicitudes-component';

@Component({
  selector: 'app-funcionario-academico-respuestas-component',
  imports: [PageComponent],
  templateUrl: './funcionario-academico-respuestas-component.html'
})
export class FuncionarioAcademicoRespuestasComponent {
  sidebar = SideBarFuncionarioAcademicoComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = FunAcadSolicitudesComponent;
}
