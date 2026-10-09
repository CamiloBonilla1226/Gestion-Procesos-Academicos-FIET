import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarDecanoComponent } from '../../components/side-bar-decano-component/side-bar-decano-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { UsuarioFietSolicitudesPrivadasComponent } from '../../../../shared/pages/usuario-fiet-solicitudes-privadas-component/usuario-fiet-solicitudes-privadas-component';

@Component({
  selector: 'app-decano-solicitudes-consejo-component',
  imports: [PageComponent],
  templateUrl: './decano-solicitudes-consejo-component.html'
})
export class DecanoSolicitudesConsejoComponent {
  sidebar = SideBarDecanoComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = UsuarioFietSolicitudesPrivadasComponent;
}
