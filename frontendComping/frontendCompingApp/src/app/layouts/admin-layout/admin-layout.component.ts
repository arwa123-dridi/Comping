import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { AdminHeaderComponent } from '../admin-header/admin-header.component';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [SidebarComponent, AdminHeaderComponent, RouterOutlet],
  template: `
    <div class="layout">
      <app-sidebar [collapsed]="sidebarCollapsed"></app-sidebar>
      <div class="main">
        <app-admin-header (sidebarToggle)="sidebarCollapsed = !sidebarCollapsed"></app-admin-header>
        <div class="content">
          <router-outlet></router-outlet>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./admin-layout.component.css']
})
export class AdminLayoutComponent {
  sidebarCollapsed = false;

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}
