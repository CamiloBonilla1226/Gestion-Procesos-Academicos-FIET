import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarUsuarioFietComponent } from '../../../usuario fiet/components/side-bar-usuario-fiet-component/side-bar-usuario-fiet-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { DocDetalleNotificacionComponent } from '../../../../shared/pages/doc-detalle-notificacion-component/doc-detalle-notificacion-component';

@Component({
  selector: 'app-docente-detalle-notificacion-component',
  imports: [PageComponent],
  templateUrl: './docente-detalle-notificacion-component.html'
})
export class DocenteDetalleNotificacionComponent {
  sidebar = SideBarUsuarioFietComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = DocDetalleNotificacionComponent;
}
