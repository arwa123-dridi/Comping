import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule} from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-header',
    standalone: true,
  imports: [CommonModule , RouterModule],
  templateUrl: './admin-header.component.html',
  styleUrl: './admin-header.component.css'
  
})
export class AdminHeaderComponent {
   @Output() sidebarToggle = new EventEmitter<void>();
 
    
  isDark = false;
  showNotif = false;
  showUserMenu = false;
  notifCount = 2;
  toggleSidebar(): void {
    this.sidebarToggle.emit();
  }
  toggleTheme(): void {
    this.isDark = !this.isDark;
    document.body.classList.toggle('dark', this.isDark);
  }
 
  toggleNotif(): void {
    this.showNotif = !this.showNotif;
    this.showUserMenu = false;
  }
 
  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
    this.showNotif = false;
  }
 
  clearNotifs(): void {
    this.notifCount = 0;
    this.showNotif = false;
  }
 
  closeAll(): void {
    this.showNotif = false;
    this.showUserMenu = false;
  }
 
  logout(): void {
    // Ajouter la logique de déconnexion ici
    console.log('Déconnexion...');
  }
}
