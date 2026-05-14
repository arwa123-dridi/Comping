import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { StatutCommande } from '../models/statut-commande';
import { Commande } from '../models/commande.model';

export interface AdresseLivraison {
  prenom: string;
  nom: string;
  telephone: string;
  adresse: string;
  ville: string;
  codePostal: string;
}

export interface CommandeRequestDTO {
  userId: string;
  adresseLivraison: AdresseLivraison;
  modePaiement: 'CARTE' | 'CASH_ON_DELIVERY';
  modeLivraison: 'HOME_DELIVERY' | 'STORE_PICKUP';
}

@Injectable({
  providedIn: 'root'
})
export class CommandeService {

  private api = 'http://localhost:8087/api/commandes';

  constructor(private http: HttpClient) { }

  // 🧾 CREATE ORDER
  createCommande(dto: CommandeRequestDTO): Observable<any> {
    return this.http.post(`${this.api}/addCommande`, dto);
  }

  updateStatut(id: string, statut: string): Observable<any> {
    return this.http.put(
      `${this.api}/updateCommande/${id}/statut?statut=${statut}`,
      {}
    );
  }
  // 📥 GET ALL ORDERS
  getAllCommandes(): Observable<Commande[]> {
    return this.http.get<any[]>(`${this.api}/getCommandes`).pipe(
      map((data: any[]): Commande[] =>
        data.map((c): Commande => ({
          id: c.id,
          userId: c.userId,
          totalProduits: c.totalProduits,
          fraisLivraison: c.fraisLivraison,
          totalCommande: c.totalCommande,
          modePaiement: c.modePaiement,
          modeLivraison: c.modeLivraison,

          statutCommande: c.statut as StatutCommande,

          dateCommande: new Date(c.dateCommande)
        }))
      )
    );
  }

  getCommandesByUser(userId: string): Observable<Commande[]> {
  return this.http.get<any[]>(`${this.api}/user/${userId}`).pipe(
    map((data: any[]): Commande[] =>
      data.map((c): Commande => ({
        id: c.id,
        userId: c.userId,
        totalProduits: c.totalProduits,
        fraisLivraison: c.fraisLivraison,
        totalCommande: c.totalCommande,
        modePaiement: c.modePaiement,
        modeLivraison: c.modeLivraison,
        statutCommande: c.statut,
        dateCommande: c.dateCommande
      }))
    )
  );
}

getCommandeById(id: string){
  return this.http.get<Commande>(`${this.api}/commandById/${id}`);
}

}