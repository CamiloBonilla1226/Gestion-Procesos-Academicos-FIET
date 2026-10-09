import { Component } from '@angular/core';
import { PageComponent } from '../../../../shared/pages/page-component/page-component';
import { SideBarEstudianteComponent } from '../../components/side-bar-estudiante-component/side-bar-estudiante-component';
import { HeaderMainComponent } from '../../../../shared/headers/header-main-component/header-main-component';
import { BreadcrumbComponent } from '../../../../shared/breadcrumb/breadcrumb-component/breadcrumb-component';
import { EstExamenSupletorioComponent } from '../../../../shared/pages/est-examen-supletorio-component/est-examen-supletorio-component';

@Component({
  selector: 'app-estudiante-examen-supletorio-component',
  imports: [PageComponent],
  templateUrl: './estudiante-examen-supletorio-component.html'
})
export class EstudianteExamenSupletorioComponent {
  sidebar = SideBarEstudianteComponent;

  header = HeaderMainComponent;

  breadcrumb = BreadcrumbComponent;

  main = EstExamenSupletorioComponent;
}
