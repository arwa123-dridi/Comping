import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { loadStripe, Stripe, StripeElements  } from '@stripe/stripe-js';
import { PaiementService } from '../services/paiement.service';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './paiement.html',
  styleUrls: ['./paiement.css']
})
export class PaiementComponent implements OnInit {

  stripe: Stripe | null = null;
  elements: StripeElements | null = null;
  stripeReady = false; 

  reservationId = '';
  paiementId    = '';
  reservation: any = null;
  paiement: any    = null;

  loading  = true;
  paying   = false;
  success  = false;
  errorMsg = '';

  private stripePublicKey = 'pk_test_51TUSnN3S7emEMZrqOm9aAIEQbNpJXJTACE1mZMzH3TbaGSBOIvRf1zHfH5TPDnREDMmDS2Pglau5MSONCqcitPAf003DW9Szbo'; // ← ta clé pk_test_

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private paiementService: PaiementService
  ) {}

  async ngOnInit() {
    this.reservationId = this.route.snapshot.paramMap.get('id') || '';

    this.http.get<any>(`http://localhost:8087/api/reservations/${this.reservationId}`)
      .subscribe({
        next: async (res) => {
          this.reservation = res;
          this.tryLoadOrCreatePaiement(res);
        },
        error: () => {
          this.errorMsg = 'Réservation introuvable.';
          this.loading = false;
        }
      });
  }

  tryLoadOrCreatePaiement(res: any) {
    this.paiementService.getByReservation(this.reservationId).subscribe({
      next: async (p) => {
        this.paiement   = p;
        this.paiementId = p.id;
        this.loading    = false;
        await this.initStripeElements(p.stripeClientSecret);
      },
      error: async () => {
        this.paiementService.createPaiement(
          this.reservationId,
          res.montantTotal || 50,
          res.modePaiement || 'CARTE'
        ).subscribe({
          next: async (p) => {
            this.paiement   = p;
            this.paiementId = p.id;
            this.loading    = false;
            await this.initStripeElements(p.stripeClientSecret);
          },
          error: (err) => {
            this.errorMsg = 'Erreur : ' + (err.error?.message || err.status);
            this.loading  = false;
          }
        });
      }
    });
  }

  async initStripeElements(clientSecret: string) {
  this.stripe = await loadStripe(this.stripePublicKey);
  if (!this.stripe) return;

  this.elements = this.stripe.elements({
    clientSecret,
    appearance: {
      theme: 'stripe',
      variables: {
        colorPrimary:    '#3da859',
        colorBackground: '#ffffff',
        colorText:       '#1b2a4a',
        colorDanger:     '#e02f2f',
        fontFamily:      '"DM Sans", Arial, sans-serif',
        borderRadius:    '10px',
      },
      rules: {
        '.Input': {
          border:    '1.5px solid rgba(27,42,74,0.15)',
          boxShadow: 'none',
          padding:   '12px 14px',
        },
        '.Input:focus': {
          border:    '1.5px solid #3da859',
          boxShadow: '0 0 0 3px rgba(61,168,89,0.12)',
        },
        '.Label': {
          fontSize:   '12px',
          fontWeight: '600',
          color:      '#8492a6',
        },
        '.Tab': {
          border:       '1.5px solid rgba(27,42,74,0.12)',
          borderRadius: '8px',
        },
        '.Tab--selected': {
          border:          '1.5px solid #3da859',
          backgroundColor: 'rgba(61,168,89,0.05)',
        },
      }
    }
  });

  const paymentElement = this.elements.create('payment', {
    layout: { type: 'tabs', defaultCollapsed: false }
  });

  setTimeout(() => {
    paymentElement.mount('#payment-element');

    // ✅ Stripe est prêt quand l'élément est monté
    paymentElement.on('ready', () => {
      this.stripeReady = true;
      console.log('✅ Stripe Payment Element prêt');
    });

    // ✅ Détecter les changements (validation)
    paymentElement.on('change', (event: any) => {
      if (event.error) {
        this.errorMsg = event.error.message;
      } else {
        this.errorMsg = '';
      }
    });

  }, 100);
}

async pay() {
  // ✅ Vérifier que Stripe est prêt
  if (!this.stripe || !this.elements || !this.stripeReady) {
    this.errorMsg = 'Le formulaire de paiement n\'est pas encore prêt. Attendez quelques secondes.';
    return;
  }

  this.paying   = true;
  this.errorMsg = '';

  const { error, paymentIntent } = await this.stripe.confirmPayment({
    elements: this.elements,
    confirmParams: {
      return_url: `http://localhost:4200/paiement-success/${this.reservationId}`,
    },
    redirect: 'if_required',
  });

  if (error) {
    this.errorMsg = error.message || 'Erreur de paiement.';
    this.paying   = false;
    return;
  }

  if (paymentIntent?.status === 'succeeded') {
    this.paiementService.validerPaiement(this.paiementId).subscribe({
      next: () => {
        this.success = true;
        this.paying  = false;
        setTimeout(() => this.router.navigate(['/reservations']), 3000);
      },
      error: () => {
        this.success = true;
        this.paying  = false;
        setTimeout(() => this.router.navigate(['/reservations']), 3000);
      }
    });
  }
}

  getNights(): number {
    if (!this.reservation?.dateDebut || !this.reservation?.dateFin) return 0;
    const diff = new Date(this.reservation.dateFin).getTime()
               - new Date(this.reservation.dateDebut).getTime();
    return Math.round(diff / (1000 * 60 * 60 * 24));
  }
}