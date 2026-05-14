import { Component } from '@angular/core';

import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,              // ⭐ VERY IMPORTANT
  imports: [RouterModule],       // because you use routerLink in sidebar

  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  isCollapsed = false;

  toggleSidebar() {
    this.isCollapsed = !this.isCollapsed;
  }
  testClick() {
    console.log('Sidebar link clicked');
  }
}
