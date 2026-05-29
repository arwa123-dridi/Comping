// src/app/layouts/sidebar/sidebar.component.ts
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  @Input() collapsed = false;
  @Output() collapsedChange = new EventEmitter<boolean>();

  userRole = '';
  userName = '';
  userInitiales = '';
  userPhoto = '';

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.loadUserInfo();
    window.addEventListener('storage', () => this.loadUserInfo());
  }

  loadUserInfo(): void {
    this.userRole  = localStorage.getItem('userRole')  ?? '';
    const prenom   = localStorage.getItem('userPrenom') ?? '';
    const nom      = localStorage.getItem('userNom')    ?? '';
    const email    = localStorage.getItem('userEmail')  ?? '';
    this.userPhoto = localStorage.getItem('userPhoto')  ?? '';
    this.userName  = (prenom || nom) ? `${prenom} ${nom}`.trim() : email.split('@')[0] || 'Utilisateur';
    this.userInitiales = this.userName.split(' ').map(w => w[0] ?? '').join('').toUpperCase().slice(0, 2) || 'U';
  }

  hasPhoto(): boolean {
    return !!this.userPhoto && this.userPhoto !== 'null' && this.userPhoto.trim() !== '';
  }

  isUser(): boolean {
    const r = this.userRole;
    return r === '' || r === 'USER' || r === 'ROLE_USER';
  }

  isOrgaOnly(): boolean {
    const r = this.userRole;
    return r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR';
  }

  isAdmin(): boolean {
    const r = this.userRole;
    return r === 'ADMIN' || r === 'ROLE_ADMIN';
  }

  isOrga(): boolean {
    return this.isOrgaOnly() || this.isAdmin();
  }

  toggleSidebar(): void {
    this.collapsed = !this.collapsed;
    this.collapsedChange.emit(this.collapsed);
  }

  logout(): void {
    ['authToken','userId','userEmail','userNom','userPrenom',
     'userRole','tokenExpiry','userPhoto'].forEach(k => localStorage.removeItem(k));
    this.router.navigate(['/login']);
  }
}