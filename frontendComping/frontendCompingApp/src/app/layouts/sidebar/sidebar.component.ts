import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SigninService } from '../../services/signin.service';
import { DemandeTransportService } from '../../services/demande-transport.service';
import { IncidentService } from '../../services/incident.service';



@Component({
  selector: 'app-sidebar',

  standalone: true,
  imports: [CommonModule, RouterModule],

  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})

export class SidebarComponent implements OnInit {
  @Input() set collapsed(v: boolean) { this.isCollapsed = v; }
  isCollapsed = false;
  role = 'USER';

  mesTransportsCount = 0;
  demandesEnAttenteCount = 0;
  incidentsOuvertsCount = 0;

  constructor(
    private signinService: SigninService,
    private router: Router,
    private demandeTransportService: DemandeTransportService,
    private incidentService: IncidentService
  ) {}


  ngOnInit(): void {
    this.role = localStorage.getItem('userRole') ?? 'USER';
    this.loadCounters();
  }

  loadCounters(): void {
    if (this.isUser()) {
      this.demandeTransportService.getMine().subscribe({
        next: (data) => {
          this.mesTransportsCount = data.filter(d => d.statut !== 'LIVREE' && d.statut !== 'ANNULEE').length;
        },
        error: () => {}
      });
    }
    if (this.isOrga()) {
      this.demandeTransportService.getAll().subscribe({
        next: (data) => {
          this.demandesEnAttenteCount = data.filter(d => d.statut === 'EN_ATTENTE').length;
        },
        error: () => {}
      });
      this.incidentService.getAll().subscribe({
        next: (data) => {
          this.incidentsOuvertsCount = data.filter(i => i.statut === 'OUVERT' || i.statut === 'EN_COURS').length;
        },
        error: () => {}
      });
    }
  }

  isAdmin(): boolean {
    return ['ADMIN','ROLE_ADMIN'].includes(this.role);
  }
  isOrga(): boolean {
    return ['ORGANISATEUR','ROLE_ORGANISATEUR'].includes(this.role) || this.isAdmin();
  }
  isUser(): boolean { return !this.isOrga(); }

  logout(): void {
    this.signinService.logout();
    this.router.navigate(['/login']);
  }
  testClick() {
    console.log('Sidebar link clicked');
  }
}
