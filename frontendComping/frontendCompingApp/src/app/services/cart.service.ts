import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private apiUrl = 'http://localhost:8087/api/panier';

  // 🟢 CART COUNT STREAM (this is what was missing)
  private cartCountSubject = new BehaviorSubject<number>(0);
  cartCount$ = this.cartCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  // 🟢 ADD PRODUCT TO CART (Backend call)
  addToCart(userId: string, produitId: string, quantite: number) {
    return this.http.post(`${this.apiUrl}/add`, {
      userId,
      produitId,
      quantite
    });
  }

  // 🟢 LOCAL CART COUNT UPDATE (for instant UI update)
  increaseCartCount() {
    const current = this.cartCountSubject.value;
    this.cartCountSubject.next(current + 1);
  }

}