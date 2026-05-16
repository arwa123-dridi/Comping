import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { AppNotification, CommunityService } from '../../services/community.service';

@Component({
  selector: 'app-community-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './community-sidebar.component.html',
  styleUrls: ['./community-sidebar.component.css']
})
export class CommunitySidebarComponent implements OnInit, OnDestroy {
  @Input() unreadMessages = 0;
  @Output() collapsedChange = new EventEmitter<boolean>();
  @Output() feedRequested = new EventEmitter<void>();
  @Output() overviewRequested = new EventEmitter<void>();

  collapsed = false;
  accessDenied = false;
  notifPanelOpen = false;
  notifications: AppNotification[] = [];
  unreadCount = 0;

  private subs: Subscription[] = [];

  constructor(public community: CommunityService, private router: Router) {}

  get isAdmin(): boolean { return this.community.isAdmin(); }

  ngOnInit(): void {
    // Garantir la connexion WebSocket quelle que soit la page affichée
    this.community.connectNotificationsSocket();

    this.subs.push(
      this.community.appNotifications$.subscribe(notifs => {
        this.notifications = notifs;
        this.unreadCount = notifs.filter(n => !n.read).length;
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  toggleNotifPanel(event: Event): void {
    event.stopPropagation();
    this.notifPanelOpen = !this.notifPanelOpen;
    if (this.notifPanelOpen) {
      this.community.markAllNotificationsRead();
    }
  }

  closeNotifPanel(): void {
    this.notifPanelOpen = false;
  }

  notifTimeAgo(date: Date): string {
    const diff = Math.floor((Date.now() - new Date(date).getTime()) / 60000);
    if (diff < 1) return 'à l\'instant';
    if (diff < 60) return `il y a ${diff} min`;
    const h = Math.floor(diff / 60);
    if (h < 24) return `il y a ${h}h`;
    return `il y a ${Math.floor(h / 24)}j`;
  }

  toggle(): void {
    this.collapsed = !this.collapsed;
    this.collapsedChange.emit(this.collapsed);
  }

  // Navigation protégée : les routes admin affichent un message d'accès refusé plutôt que de rediriger silencieusement
  tryNav(route: string, adminOnly = false): void {
    if (adminOnly && !this.isAdmin) {
      this.accessDenied = true;
      setTimeout(() => this.accessDenied = false, 3500);
      return;
    }
    void this.router.navigate([route]);
  }

  onAdminClick(event: Event, route: string): void {
    if (!this.isAdmin) {
      event.preventDefault();
      this.accessDenied = true;
      setTimeout(() => this.accessDenied = false, 3500);
    } else {
      void this.router.navigate([route], { queryParams: { tab: 'admin' } });
    }
  }

  openFeed(event: Event): void {
    event.preventDefault();
    this.feedRequested.emit();
    void this.router.navigate(['/community']);
  }

  // L'overview (tableau de bord modération) est émis via Output pour que le parent (PostsFeedComponent) l'affiche en inline — pas une route séparée
  openOverview(event: Event): void {
    event.preventDefault();
    if (!this.isAdmin) {
      this.accessDenied = true;
      setTimeout(() => this.accessDenied = false, 3500);
      return;
    }
    this.overviewRequested.emit();
    void this.router.navigate(['/community'], { queryParams: { view: 'overview' } });
  }

  logout(): void {
    this.community.disconnectNotificationsSocket();
    localStorage.removeItem('authToken');
    localStorage.removeItem('token');
    void this.router.navigate(['/social-home']);
  }

  initials(): string {
    const email = this.community.getCurrentEmail() || '';
    return email.split(/[@.\s]/).filter(Boolean).slice(0, 2).map(p => p[0]).join('').toUpperCase() || 'C';
  }
}
