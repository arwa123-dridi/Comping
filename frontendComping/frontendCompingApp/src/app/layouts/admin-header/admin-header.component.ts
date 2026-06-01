import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SigninService } from '../../services/signin.service';

@Component({
  selector: 'app-admin-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-header.component.html',
  styleUrl: './admin-header.component.css'
})
export class AdminHeaderComponent implements OnInit {

  @Output() sidebarToggle = new EventEmitter<void>();

  isDark = false;
  showNotif = false;
  showUserMenu = false;
  notifCount = 2;

  userName = 'Utilisateur';
  userEmail = '';
  userRole = '';
  userInitiales = 'U';

  constructor(
    private signinService: SigninService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const nom    = localStorage.getItem('userNom')    ?? '';
    const prenom = localStorage.getItem('userPrenom') ?? '';

    this.userEmail = localStorage.getItem('userEmail') ?? '';
    this.userRole  = localStorage.getItem('userRole')  ?? '';

    this.userName = (prenom || nom)
      ? `${prenom} ${nom}`.trim()
      : (this.userEmail.split('@')[0] || 'Utilisateur');

    this.userInitiales = this.userName
      .split(' ')
      .map(w => w[0])
      .join('')
      .toUpperCase()
      .slice(0, 2) || 'U';
  }

  getRoleLabel(): string {
    const r = this.userRole;
    if (r === 'ADMIN' || r === 'ROLE_ADMIN') return 'Administrateur';
    if (r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR') return 'Organisateur';
    return 'Utilisateur';
  }

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
    this.signinService.logout();
    this.router.navigate(['/login']);
  }
}
