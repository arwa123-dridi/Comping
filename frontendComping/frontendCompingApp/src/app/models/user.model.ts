export interface User {
  id: string;
  firstName: string;
  lastName: string;
  nom?: string;
  prenom?: string;
  email: string;
  telephone?: string;
  address?: string;
  role: string;
  photo?: string;
  statut?: boolean;
  actif?: boolean;
}