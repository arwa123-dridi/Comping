import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

// 👇 imports des composants
import { SidebarComponent } from '../sidebar/sidebar.component';
import { AdminHeaderComponent } from '../admin-header/admin-header.component';

@Component({
  selector: 'app-admin-layout',
  standalone: true, // ✅ IMPORTANT
  imports: [
    SidebarComponent,
    AdminHeaderComponent,
    RouterOutlet
  ],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.css'] // ✅ (s)
})
export class AdminLayoutComponent {}