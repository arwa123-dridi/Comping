import { Component, EventEmitter, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Subscription, interval, startWith, switchMap } from 'rxjs';
import { SigninService } from '../../services/signin.service';
import { NotificationService } from '../../services/notification.service';
import { NotificationResponse } from '../../models/notification.model';

@Component({
  selector: 'app-admin-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-header.component.html',
  styleUrl: './admin-header.component.css'
})
export class AdminHeaderComponent implements OnInit, OnDestroy {
  @Output() sidebarToggle = new EventEmitter<void>();

  isDark = false;
  showNotif = false;
  showUserMenu = false;
  notifCount = 0;
  notifications: NotificationResponse[] = [];
  private pollSub?: Subscription;

  // Dynamic user info from localStorage
  userName = 'Utilisateur';
  userEmail = '';
  userRole = '';
  userInitiales = 'U';

  constructor(
    private signinService: SigninService,
    private router: Router,
    private notificationService: NotificationService
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

    this.pollSub = interval(30000).pipe(
      startWith(0),
      switchMap(() => this.notificationService.unreadCount())
    ).subscribe({
      next: (res) => this.notifCount = res.count,
      error: () => {}
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  getRoleLabel(): string {
    const r = this.userRole;
    if (r === 'ADMIN' || r === 'ROLE_ADMIN') return 'Administrateur';
    if (r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR') return 'Organisateur';
    return 'Utilisateur';
  }

  toggleSidebar(): void { this.sidebarToggle.emit(); }

  toggleTheme(): void {
    this.isDark = !this.isDark;
    document.body.classList.toggle('dark', this.isDark);
  }

  toggleNotif(): void {
    this.showNotif = !this.showNotif;
    this.showUserMenu = false;
    if (this.showNotif) {
      this.notificationService.getMine().subscribe({
        next: (data) => this.notifications = data,
        error: () => {}
      });
    }
  }

  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
    this.showNotif = false;
  }

  selectNotif(n: NotificationResponse): void {
    if (!n.lu) {
      this.notificationService.markRead(n.id).subscribe(() => {
        n.lu = true;
        this.notifCount = Math.max(0, this.notifCount - 1);
      });
    }
    this.closeAll();
    if (n.lien) this.router.navigateByUrl(n.lien);
  }

  clearNotifs(): void {
    this.notificationService.markAllRead().subscribe(() => {
      this.notifications.forEach(n => n.lu = true);
      this.notifCount = 0;
    });
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
