import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { SidebarComponent } from '../../../../shared/sidebars/sidebar-component/sidebar-component';

@Component({
  selector: 'app-side-bar-decano-component',
  imports: [SidebarComponent],
  templateUrl: './side-bar-decano-component.html'
})
export class SideBarDecanoComponent {
  constructor(private router: Router) {}

  onSidebarButtonClick(route: string) {
    this.router.navigate([route]);
  }
}
