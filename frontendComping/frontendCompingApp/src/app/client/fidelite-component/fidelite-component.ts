import { Component, OnInit } from '@angular/core';
import { CarteFedeliteEvent} from '../../services/carte-fedelite-event';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  imports: [CommonModule],
  selector: 'app-fidelite-component',
  templateUrl: './fidelite-component.html',
  styleUrl: './fidelite-component.css',
})
export class FideliteComponent implements OnInit {

  message: string = '';
  userId: string = '';
showPopup = false;
  constructor(private carteService: CarteFedeliteEvent) {}

 ngOnInit() {

  const token = localStorage.getItem('authToken');

  console.log("TOKEN:", token);

  if (!token) {
    return;
  }

  try {

    const payload = JSON.parse(atob(token.split('.')[1]));

    console.log(payload);

    // récupération id
   this.userId = payload.sub;

    console.log("ID USER =", this.userId);

  } catch (e) {
    console.error("Erreur token", e);
    return;
  }

  this.carteService.getMessage(this.userId)
    .subscribe({

      next: (res: string) => {

        console.log("MESSAGE BACK =", res);

        this.message = res;

        this.showPopup = true;

        setTimeout(() => {
          this.showPopup = false;
        }, 5000);
      },

      error: (err) => {
        console.error(err);
      }
    });
}
  getUserIdFromToken(): string | null {
  const token = localStorage.getItem('authtoken');
  if (!token) return null;

  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.id || payload.sub;  // selon ton backend
}
}