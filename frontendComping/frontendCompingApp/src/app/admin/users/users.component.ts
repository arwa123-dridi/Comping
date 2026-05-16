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
activeCount = 0;
adminCount = 0;
inactiveCount = 0;
  filterNom = '';
  filterPrenom = '';
  filterRole = '';

  userToDelete: User | null = null;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  // Charger les utilisateurs
  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.filteredUsers = [...this.users];
      },
      error: (err) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
      }
    });
  }

  // Filtrage
  filterUsers(): void {
    const nom = this.filterNom.toLowerCase().trim();
    const prenom = this.filterPrenom.toLowerCase().trim();
    const role = this.filterRole;

    this.filteredUsers = this.users.filter(u => {
      const matchNom = !nom || u.lastName.toLowerCase().includes(nom);
      const matchPrenom = !prenom || u.firstName.toLowerCase().includes(prenom);
      const matchRole = !role || u.role === role;
      return matchNom && matchPrenom && matchRole;
    });
      this.activeCount   = this.filteredUsers.filter(u => u.statut).length;
  this.inactiveCount = this.filteredUsers.filter(u => !u.statut).length;
  this.adminCount    = this.filteredUsers.filter(u => u.role === 'ADMIN').length;
  }

  // Reset filtres
  resetFilters(): void {
    this.filterNom = '';
    this.filterPrenom = '';
    this.filterRole = '';
    this.filteredUsers = [...this.users];
  }

  // Ouvrir modal
  deleteUser(user: User): void {
    this.userToDelete = user;
  }

  // Confirmer suppression
 confirmDelete(): void {
  if (!this.userToDelete) return;

  const id = this.userToDelete.id;

  this.userService.deleteUser(id).subscribe({
    next: () => {
      // 1. Mettre à jour la liste
      this.users = this.users.filter(u => u.id !== id);

      // 2. Réappliquer les filtres
      this.filterUsers();

      // 3. Forcer la fermeture de la modal
    setTimeout(() => {
    this.userToDelete = null;
  });
    },
    error: (err) => {
      console.error('Erreur lors de la suppression', err);
    }
  });
}
  // Annuler
  cancelDelete(): void {
    this.userToDelete = null;
  }


  toggleStatus(user: User): void {
  const updatedStatus = !user.statut;

  this.userService.updateUserStatus(user.id, updatedStatus).subscribe({
    next: () => {
      user.statut = updatedStatus;
    },
    error: (err) => {
      console.error('Erreur lors du changement de statut', err);
    }
  });
}
getAvatarColor(user: any): string {
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
}