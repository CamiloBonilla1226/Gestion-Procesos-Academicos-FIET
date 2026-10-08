import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarFuncionarioAcademicoComponent } from '../../components/side-bar-funcionario-academico-component/side-bar-funcionario-academico-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { InfoUsuarioComponent } from '../../../../shared/pages/info-usuario-component/info-usuario-component';

@Component({
  selector: 'app-funcionario-academico-home-component',
  imports: [PageComponent],
  templateUrl: './funcionario-academico-home-component.html'
})
export class FuncionarioAcademicoHomeComponent {
  sidebar = SideBarFuncionarioAcademicoComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = InfoUsuarioComponent;
}
