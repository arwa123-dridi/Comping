import { AfterViewInit, Component, ElementRef, ViewChild, OnInit } from '@angular/core';
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
export class SignupComponent implements AfterViewInit, OnInit {
  @ViewChild('slidesWrapper') slidesWrapper!: ElementRef<HTMLDivElement>;
  slides = [
    { url: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470' },
    { url: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e' },
    { url: 'https://images.unsplash.com/photo-1501700493785-59a0ab2ec3e2' },
  ];

  currentSlide = 0;
  private intervalId: any;
  signupForm: FormGroup;

  successMessage: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;
  showPassword: boolean = false;

  roles = [
   // 'Select role',
    'ADMIN',
    'PROPRIETAIRE_SITE',
    'BOUTIQUE',
    'ORGANISATEUR',
    'PARTENAIRE_logistique',
    'MODERATEUR',
    'USER'
  ];


constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.signupForm = this.fb.group({
      FirstName: ['', Validators.required],
      LastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      telephone: [''],
      address: [''],
      role: [null, Validators.required]   // <-- initialize as null
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;

    if (password && confirmPassword && password !== confirmPassword) {
      form.get('confirmPassword')?.setErrors({ mismatch: true });
    } else {
      form.get('confirmPassword')?.setErrors(null);
    }
  }

  ngAfterViewInit() {
    this.startSlideshow();
  }

  ngOnDestroy() {
    clearInterval(this.intervalId);
  }

  nextSlide() {
    if (!this.slidesWrapper) return;
    this.currentSlide = (this.currentSlide + 1) % this.slides.length;
    this.slidesWrapper.nativeElement.style.transform = `translateX(-${this.currentSlide * 100}%)`;
  }
  startSlideshow() {
    this.intervalId = setInterval(() => {
      this.nextSlide();
    }, 4000);
  }

  ngOnInit() {
    this.signupForm.valueChanges.subscribe(() => {
      this.passwordMatchValidator(this.signupForm);
    });
  }

  get f() {
    return this.signupForm.controls;
  }

  onSubmit(): void {

    console.log('Form submitted'); // <-- log when submission starts
    console.log('Form value:', this.signupForm.value); // <-- log raw form values

    if (!this.signupForm.value.role) {
      this.errorMessage = 'Veuillez sélectionner un rôle.';
      console.log('Error: role not selected'); // <-- log missing role
      return;
    }


    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      console.log('Error: form invalid'); // <-- log validation errors
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

// Clean optional fields (avoid sending empty strings)
    const formValue = this.signupForm.value;

    const signupData = {
      firstName: formValue.FirstName,
      lastName: formValue.LastName,
      email: formValue.email,
      password: formValue.password,
      telephone: formValue.telephone || null,
      address: formValue.address || null,
      role: formValue.role
    };

    console.log('Signup data being sent to backend:', signupData); // <-- log cleaned data

    const headers = { 'Content-Type': 'application/json' };
    this.http.post<{ id: string, email: string, role: string }>('http://localhost:8087/api/auth/registerUser', signupData, { headers })
      .subscribe({
        next: (res) => {
          console.log('Backend response:', res);
          localStorage.setItem('userId', res.id);
          localStorage.setItem('userEmail', res.email);
          localStorage.setItem('userRole', res.role || 'USER');
          localStorage.setItem('userNom', `${formValue.FirstName} ${formValue.LastName}`);
          
          this.successMessage = '🎉 Inscription réussie ! Vous pouvez maintenant vous connecter.';
          this.signupForm.reset({ role: null });
          this.isLoading = false;
          setTimeout(() => this.router.navigate(['/signin']), 2000);
        },
        error: (err) => {
          console.error('Signup failed', err);
          this.errorMessage = err.error || 'Erreur inscription. Email existe peut-être déjà.';
          this.isLoading = false;
        }
      });
  }
}