import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent implements OnInit {

  users: User[] = [];
  filteredUsers: User[] = [];
  activeCount   = 0;
  adminCount    = 0;
  inactiveCount = 0;
  filterNom    = '';
  filterPrenom = '';
  filterRole   = '';
  userToDelete: User | null = null;

  constructor(private userService: UserService) {}

  ngOnInit(): void { this.loadUsers(); }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (data: any[]) => {
        // ✅ Normalise les champs backend (nom/prenom OU firstName/lastName)
        this.users = data.map(u => ({
          id:        u.id        ?? u._id ?? '',
          firstName: u.firstName ?? u.prenom ?? '',
          lastName:  u.lastName  ?? u.nom   ?? '',
          email:     u.email     ?? '',
          telephone: u.telephone ?? '',
          address:   u.address   ?? u.adresse ?? '',
          role:      u.role      ?? 'USER',
          photo:     u.photo     ?? '',
          // ✅ statut = actif OU statut selon le backend
          statut:    u.statut !== undefined ? u.statut : (u.actif !== undefined ? u.actif : true),
          actif:     u.actif  !== undefined ? u.actif  : (u.statut !== undefined ? u.statut : true),
        } as User));
        this.filteredUsers = [...this.users];
        // ✅ Calculer les compteurs au chargement
        this.updateCounts();
      },
      error: (err: any) => console.error('Erreur chargement utilisateurs', err)
    });
  }

  // ✅ Compteurs mis à jour à chaque filtre ET au chargement
  updateCounts(): void {
    this.activeCount   = this.filteredUsers.filter(u => u.statut || u.actif).length;
    this.inactiveCount = this.filteredUsers.filter(u => !u.statut && !u.actif).length;
    this.adminCount    = this.filteredUsers.filter(u =>
      u.role === 'ADMIN' || u.role === 'ROLE_ADMIN'
    ).length;
  }

  filterUsers(): void {
    const nom    = this.filterNom.toLowerCase().trim();
    const prenom = this.filterPrenom.toLowerCase().trim();
    const role   = this.filterRole;

    this.filteredUsers = this.users.filter(u => {
      const matchNom    = !nom    || u.lastName.toLowerCase().includes(nom);
      const matchPrenom = !prenom || u.firstName.toLowerCase().includes(prenom);
      const matchRole   = !role   || u.role === role || u.role === `ROLE_${role}`;
      return matchNom && matchPrenom && matchRole;
    });
    this.updateCounts();
  }

  resetFilters(): void {
    this.filterNom = '';
    this.filterPrenom = '';
    this.filterRole = '';
    this.filteredUsers = [...this.users];
    this.updateCounts();
  }

  // ✅ Toggle statut — envoie les 2 champs pour compatibilité backend
  toggleStatus(user: User): void {
    const newStatut = !(user.statut ?? user.actif ?? true);
    // Envoie { statut } ET { actif } pour couvrir les 2 conventions backend
    this.userService.updateUserStatus(user.id, newStatut).subscribe({
      next: () => {
        user.statut = newStatut;
        user.actif  = newStatut;
        this.updateCounts();
      },
      error: (err: any) => console.error('Erreur changement statut', err)
    });
  }

  isActif(user: User): boolean {
    return user.statut !== undefined ? !!user.statut : !!user.actif;
  }

  deleteUser(user: User): void { this.userToDelete = user; }
  cancelDelete(): void { this.userToDelete = null; }

  confirmDelete(): void {
    if (!this.userToDelete) return;
    const id = this.userToDelete.id;
    this.userService.deleteUser(id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== id);
        this.filterUsers();
        setTimeout(() => { this.userToDelete = null; });
      },
      error: (err: any) => console.error('Erreur suppression', err)
    });
  }

  getAvatarColor(user: User): string {
    const colors = [
      'linear-gradient(135deg,#6366f1,#8b5cf6)',
      'linear-gradient(135deg,#0ea5e9,#0284c7)',
      'linear-gradient(135deg,#f59e0b,#d97706)',
      'linear-gradient(135deg,#10b981,#059669)',
      'linear-gradient(135deg,#f43f5e,#e11d48)',
    ];
    const i = (user.firstName?.charCodeAt(0) || 0) % colors.length;
    return colors[i];
  }

  getInitiales(user: User): string {
    const f = user.firstName?.[0] ?? '';
    const l = user.lastName?.[0]  ?? '';
    return (f + l).toUpperCase() || '?';
  }

  getRoleLabel(role: string): string {
    if (role === 'ADMIN' || role === 'ROLE_ADMIN') return 'ADMIN';
    if (role === 'ORGANISATEUR' || role === 'ROLE_ORGANISATEUR') return 'ORGANISATEUR';
    return 'USER';
  }
}
