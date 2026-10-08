import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { SidebarComponent } from '../../../../shared/sidebars/sidebar-component/sidebar-component';

@Component({
  selector: 'app-side-bar-estudiante-component',
  imports: [SidebarComponent],
  templateUrl: './side-bar-estudiante-component.html'
})
export class SideBarEstudianteComponent {
  constructor(private router: Router) {}

  onSidebarButtonClick(route: string) {
    this.router.navigate([route]);
  }
}
