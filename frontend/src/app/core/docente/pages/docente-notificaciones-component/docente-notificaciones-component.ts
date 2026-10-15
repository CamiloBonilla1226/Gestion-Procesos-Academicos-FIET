import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarUsuarioFietComponent } from '../../../usuario fiet/components/side-bar-usuario-fiet-component/side-bar-usuario-fiet-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { DocNotificacionesComponent } from '../../../../shared/pages/doc-notificaciones-component/doc-notificaciones-component';

@Component({
  selector: 'app-docente-notificaciones-component',
  imports: [PageComponent],
  templateUrl: './docente-notificaciones-component.html'
})
export class DocenteNotificacionesComponent {
  sidebar = SideBarUsuarioFietComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = DocNotificacionesComponent;
}
