import { CommonModule } from '@angular/common';
import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { SigninService } from '../../services/signin.service';

interface AdminCommand {
  label: string;
  description: string;
  path: string;
  keywords: string[];
}

@Component({
  selector: 'app-admin-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-header.component.html',
  styleUrl: './admin-header.component.css'
})
export class AdminHeaderComponent implements OnInit {
  @Output() sidebarToggle = new EventEmitter<void>();
  @ViewChild('searchInput') searchInput?: ElementRef<HTMLInputElement>;

  readonly commands: AdminCommand[] = [
    {
      label: 'Dashboard',
      description: 'Vue d’ensemble et centre d’opérations',
      path: '/admin/dashboard',
      keywords: ['accueil', 'dashboard', 'overview', 'operations']
    },
    {
      label: 'Sécurité',
      description: 'Mesures de sécurité et monitoring',
      path: '/admin/securite',
      keywords: ['securite', 'monitoring', 'surveillance', 'risque']
    },
    {
      label: 'Urgences',
      description: 'Gestion des urgences et affectations',
      path: '/admin/urgences',
      keywords: ['urgence', 'dispatch', 'assignation', 'escalade']
    },
    {
      label: 'Incidents',
      description: 'Cycle de vie des incidents',
      path: '/admin/incidents',
      keywords: ['incident', 'rapport', 'resolve', 'ferme']
    },
    {
      label: 'Alertes',
      description: 'Alertes, notifications et diffusion',
      path: '/admin/alertes',
      keywords: ['alertes', 'broadcast', 'notifications', 'bell']
    },
    {
      label: 'IA Chat',
      description: 'Assistant IA sécurité et camping',
      path: '/admin/ai-chatbot',
      keywords: ['ia', 'chatbot', 'assistant', 'ollama']
    },
    {
      label: 'Météo',
      description: 'Données météo et alertes par position',
      path: '/admin/dashboard?panel=weather',
      keywords: ['weather', 'meteo', 'forecast', 'pluie', 'vent']
    },
    {
      label: 'Cartographie',
      description: 'Services proches, géolocalisation et itinéraires',
      path: '/admin/dashboard?panel=maps',
      keywords: ['maps', 'map', 'route', 'location', 'geofence']
    },
    {
      label: 'Workflows IA',
      description: 'Escalade, analyse prédictive et automatisations',
      path: '/admin/dashboard?panel=workflows',
      keywords: ['workflow', 'predictif', 'ai', 'broadcast', 'dispatch']
    }
  ];

  isDark = false;
  showNotif = false;
  showUserMenu = false;
  showSearchResults = false;
  notifCount = 2;
  searchQuery = '';
  filteredCommands: AdminCommand[] = this.commands.slice(0, 6);

  constructor(private router: Router, private signinService: SigninService) {}

  ngOnInit(): void {
    this.isDark = localStorage.getItem('campino-theme') === 'dark';
    document.body.classList.toggle('dark', this.isDark);
  }

  toggleSidebar(): void {
    this.sidebarToggle.emit();
  }

  toggleTheme(): void {
    this.isDark = !this.isDark;
    document.body.classList.toggle('dark', this.isDark);
    localStorage.setItem('campino-theme', this.isDark ? 'dark' : 'light');
  }

  toggleNotif(): void {
    this.showNotif = !this.showNotif;
    this.showUserMenu = false;
    this.showSearchResults = false;
  }

  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
    this.showNotif = false;
    this.showSearchResults = false;
  }

  clearNotifs(): void {
    this.notifCount = 0;
    this.showNotif = false;
  }

  onSearchFocus(): void {
    this.showNotif = false;
    this.showUserMenu = false;
    this.showSearchResults = true;
    this.updateSearchResults(this.searchQuery);
  }

  onSearchInput(value: string): void {
    this.searchQuery = value;
    this.showSearchResults = true;
    this.updateSearchResults(value);
  }

  executeSearch(): void {
    const firstMatch = this.filteredCommands[0];
    if (firstMatch) {
      this.navigateToCommand(firstMatch);
    }
  }

  navigateToCommand(command: AdminCommand): void {
    this.searchQuery = '';
    this.showSearchResults = false;

    if (command.path.includes('?')) {
      const [path, queryString] = command.path.split('?');
      const queryParams = Object.fromEntries(new URLSearchParams(queryString).entries());
      this.router.navigate([path], { queryParams });
      return;
    }

    this.router.navigate([command.path]);
  }

  private updateSearchResults(value: string): void {
    const normalized = value.trim().toLowerCase();
    if (!normalized) {
      this.filteredCommands = this.commands.slice(0, 6);
      return;
    }

    this.filteredCommands = this.commands
      .filter(command =>
        command.label.toLowerCase().includes(normalized) ||
        command.description.toLowerCase().includes(normalized) ||
        command.keywords.some(keyword => keyword.toLowerCase().includes(normalized))
      )
      .slice(0, 6);
  }

  closeAll(): void {
    this.showNotif = false;
    this.showUserMenu = false;
    this.showSearchResults = false;
  }

  logout(): void {
    console.log('Déconnexion...');
    this.signinService.logout();
    this.router.navigate(['/login']);
  }

  @HostListener('window:keydown', ['$event'])
  handleShortcuts(event: KeyboardEvent): void {
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      this.searchInput?.nativeElement.focus();
      this.onSearchFocus();
    }

    if (event.key === 'Escape') {
      this.closeAll();
    }
  }
}
