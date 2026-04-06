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
}