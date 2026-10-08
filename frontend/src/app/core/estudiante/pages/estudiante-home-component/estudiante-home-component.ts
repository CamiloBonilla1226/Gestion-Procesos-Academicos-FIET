import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarEstudianteComponent } from '../../components/side-bar-estudiante-component/side-bar-estudiante-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { InfoUsuarioComponent } from '../../../../shared/pages/info-usuario-component/info-usuario-component';

@Component({
  selector: 'app-estudiante-home-component',
  imports: [PageComponent],
  templateUrl: './estudiante-home-component.html'
})
export class EstudianteHomeComponent {
  sidebar = SideBarEstudianteComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = InfoUsuarioComponent;
}
