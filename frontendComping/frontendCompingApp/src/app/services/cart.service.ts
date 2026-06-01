import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CartService {

  private apiUrl = 'http://localhost:8087/api/panier';

  private cartCountSubject = new BehaviorSubject<number>(0);
  cartCount$ = this.cartCountSubject.asObservable();

  private cartSubject = new BehaviorSubject<any>(null);
  cart$ = this.cartSubject.asObservable();

  constructor(private http: HttpClient) { }

  private getHeaders() {
    const token = localStorage.getItem('authToken');
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
      })
    };
  }

  // 🟢 ADD PRODUCT
  addToCart(userId: string, produitId: string, quantite: number) {

    const body = {
      userId,
      lignes: [{ produitId, quantite }]
    };

    console.log("📦 SENDING BODY:", body);

    return this.http.post(`${this.apiUrl}/add`, body, this.getHeaders())
      .pipe(
        tap((res) => {
          console.log("✅ ADD RESPONSE:", res);

          this.cartCountSubject.next(this.cartCountSubject.value + quantite);

          this.refreshCartCount(userId);
        })
      );
  }

  // 🟢 GET PANIER
  getCart(userId: string) {
    return this.http.get(`${this.apiUrl}/${userId}`, this.getHeaders());
  }

  // 🟢 REMOVE PRODUCT
  removeProduct(userId: string, produitId: string) {
    return this.http.delete(`${this.apiUrl}/${userId}/${produitId}`, this.getHeaders())
      .pipe(
        tap(() => this.refreshCartCount(userId))
      );
  }

  // 🟢 UPDATE QUANTITY
  updateQuantity(userId: string, produitId: string, quantity: number) {
    return this.http.put(
      `${this.apiUrl}/update?userId=${userId}&produitId=${produitId}&quantity=${quantity}`,
      {},
      this.getHeaders()
    ).pipe(
      tap(() => this.refreshCartCount(userId))
    );
  }

  refreshCartCount(userId: string) {
  this.getCart(userId).subscribe({
    next: (cart: any) => {

      // 🔥 UPDATE FULL CART STATE
      this.cartSubject.next(cart);

      if (!cart?.lignes) {
        this.cartCountSubject.next(0);
        return;
      }

      const totalQty = cart.lignes.reduce(
        (sum: number, item: any) => sum + item.quantite,
        0
      );

      this.cartCountSubject.next(totalQty);
    },
    error: () => {
      this.cartCountSubject.next(0);
      this.cartSubject.next(null);
    }
  });
}

  clearCart(userId: string) {
    return this.http.delete(`${this.apiUrl}/clear/${userId}`, this.getHeaders());
  }

  forceRefreshCart(userId: string) {
    this.getCart(userId).subscribe({
      next: (cart: any) => {

        if (!cart?.lignes) {
          this.cartCountSubject.next(0);
          return;
        }

        const totalQty = cart.lignes.reduce(
          (sum: number, item: any) => sum + item.quantite,
          0
        );

        this.cartCountSubject.next(totalQty);
      },
      error: () => this.cartCountSubject.next(0)
    });
  }

  

}