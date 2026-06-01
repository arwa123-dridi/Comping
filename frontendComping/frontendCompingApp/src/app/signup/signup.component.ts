import { AfterViewInit, Component, ElementRef, ViewChild, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule, NgIf } from '@angular/common';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements AfterViewInit, OnInit, OnDestroy {

  @ViewChild('slidesWrapper') slidesWrapper!: ElementRef<HTMLDivElement>;

  slides = [
    { url: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470' },
    { url: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e' },
    { url: 'https://images.unsplash.com/photo-1501700493785-59a0ab2ec3e2' },
  ];

  currentSlide = 0;
  private intervalId: any;
  signupForm: FormGroup;

  successMessage = '';
  errorMessage   = '';
  isLoading      = false;
  showPassword   = false;
  showSuccessPopup = false;

  roles = [
    'ADMIN',
    'PROPRIETAIRE_SITE',
    'BOUTIQUE',
    'ORGANISATEUR',
    'PARTENAIRE_logistique',
    'USER',
    'LIVREUR'
  ];

  constructor(
    private fb:     FormBuilder,
    private http:   HttpClient,
    private cd:     ChangeDetectorRef,
    private router: Router
  ) {
    this.signupForm = this.fb.group({
      firstName:       ['', Validators.required],
      lastName:        ['', Validators.required],
      email:           ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9._%+-]+@gmail\.com$/)]],
      password:        ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      telephone:       ['+216', [Validators.pattern(/^\+216\d{8}$/)]],
      address:         [''],
      role:            [null, Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    if (!password || !confirmPassword) return;
    if (password !== confirmPassword) {
      form.get('confirmPassword')?.setErrors({ mismatch: true });
    } else {
      const errors = form.get('confirmPassword')?.errors;
      if (errors) {
        delete errors['mismatch'];
        if (Object.keys(errors).length === 0) {
          form.get('confirmPassword')?.setErrors(null);
        }
      }
    }
  }

  ngOnInit() {
    this.signupForm.valueChanges.subscribe(() => {
      this.passwordMatchValidator(this.signupForm);
    });
  }

  ngAfterViewInit() { this.startSlideshow(); }

  ngOnDestroy() { clearInterval(this.intervalId); }

  nextSlide() {
    if (!this.slidesWrapper) return;
    this.currentSlide = (this.currentSlide + 1) % this.slides.length;
    this.slidesWrapper.nativeElement.style.transform = `translateX(-${this.currentSlide * 100}%)`;
  }

  startSlideshow() {
    this.intervalId = setInterval(() => this.nextSlide(), 4000);
  }

  get f() { return this.signupForm.controls; }

  onSubmit(): void {
    console.log('Form submitted');
    console.log('Form value:', this.signupForm.value);

    if (!this.signupForm.value.role) {
      this.errorMessage = 'Veuillez sélectionner un rôle.';
      console.log('Error: role not selected');
      return;
    }

    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      console.log('Error: form invalid');
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const formValue = this.signupForm.value;

    const signupData = {
      firstName: formValue.firstName,
      lastName:  formValue.lastName,
      email:     formValue.email,
      password:  formValue.password,
      telephone: formValue.telephone || null,
      address:   formValue.address   || null,
      role:      formValue.role
    };

    console.log('Signup data being sent to backend:', signupData);

    const headers = { 'Content-Type': 'application/json' };
    this.http.post<{ id: string, email: string, role: string }>(
      'http://localhost:8087/api/auth/registerUser',
      signupData,
      { headers }
    ).subscribe({
      next: (res) => {
        console.log('Backend response:', res);
        this.isLoading = false;

        localStorage.setItem('userId',    res.id);
        localStorage.setItem('userEmail', res.email);
        localStorage.setItem('userRole',  res.role || 'USER');
        localStorage.setItem('userNom',   `${formValue.firstName} ${formValue.lastName}`.trim());

        this.showSuccessPopup = true;
        this.successMessage   = '🎉 Inscription réussie ! Redirection vers la connexion...';
        this.cd.detectChanges();

        this.signupForm.reset({ role: null });

        setTimeout(() => {
          this.showSuccessPopup = false;
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        console.error('Signup failed', err);
        this.errorMessage = err.error?.message || err.error || 'Erreur inscription. Email existe peut-être déjà.';
        this.isLoading = false;
      }
    });
  }

  onPhoneInput(event: any) {
    let value = event.target.value;
    if (!value.startsWith('+216')) {
      value = '+216' + value.replace(/\D/g, '');
    }
    const digits = value.replace('+216', '').replace(/\D/g, '').slice(0, 8);
    this.signupForm.get('telephone')?.setValue('+216' + digits, { emitEvent: false });
  }

  onEmailInput(event: any) {
    const value = event.target.value.toLowerCase();
    this.signupForm.get('email')?.setValue(value, { emitEvent: false });
  }

  closePopup() {
    this.showSuccessPopup = false;
  }
}
