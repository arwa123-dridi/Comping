import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Product {
  id: string;
  nomProduit: string;
  descriptionProduit: string;
  prixProduit: number;
  categorieProduit: string;
  statut: string;
  imageUrl: string;
  quantiteStock?: number;
  seuilAlerteStock?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private api = 'http://localhost:8087/api/produits';

  constructor(private http: HttpClient) {}

  // ================= GET PRODUCT BY ID =================
  getProductById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.api}/${id}`);
  }
}