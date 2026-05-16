<<<<<<< HEAD
import { AfterViewInit, Component, ElementRef, ViewChild, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
=======
import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
>>>>>>> origin/ahmed
import { CommonModule, NgIf } from '@angular/common';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-signup',
<<<<<<< HEAD
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements AfterViewInit, OnInit {
=======
  //standalone: true,
  imports: [ReactiveFormsModule, HttpClientModule, CommonModule, NgIf],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements AfterViewInit {
>>>>>>> origin/ahmed
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
  showSuccessPopup: boolean = false;
  roles = [
    // 'Select role',
    'ADMIN',
    'PROPRIETAIRE_SITE',
    'BOUTIQUE',
    'ORGANISATEUR',
    'PARTENAIRE_logistique',
<<<<<<< HEAD
    'USER',
    'LIVREUR'
  ];



  constructor(private fb: FormBuilder, private http: HttpClient, private cd: ChangeDetectorRef,private router: Router) {

=======
    'USER'
  ];


  constructor(private fb: FormBuilder, private http: HttpClient, private cd: ChangeDetectorRef) {
>>>>>>> origin/ahmed

    this.signupForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: [
        '',
        [
          Validators.required,
          Validators.pattern(/^[a-zA-Z0-9._%+-]+@gmail\.com$/)
        ]
      ], password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      telephone: [
        '+216',
        [
          Validators.pattern(/^\+216\d{8}$/)
        ]
      ],
      address: [''],
      role: [null, Validators.required]   // <-- initialize as null
    },
  { validators: this.passwordMatchValidator } );
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

<<<<<<< HEAD
ngOnInit() {
    this.signupForm.valueChanges.subscribe(() => {
      this.passwordMatchValidator(this.signupForm);
    });
=======


  get f() {
    return this.signupForm.controls;
>>>>>>> origin/ahmed
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

<<<<<<< HEAD
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
=======
    // Clean optional fields (avoid sending empty strings)
    const formValue = this.signupForm.value;

    const signupData = {
      ...formValue,
      telephone: formValue.telephone || null,
      address: formValue.address || null
>>>>>>> origin/ahmed
    };

    console.log('Signup data being sent to backend:', signupData); // <-- log cleaned data

<<<<<<< HEAD
    const headers = { 'Content-Type': 'application/json' };
    this.http.post<{ id: string, email: string, role: string }>('http://localhost:8087/api/auth/registerUser', signupData, { headers })
=======
    this.http.post('http://localhost:8087/api/auth/registerUser', signupData)
>>>>>>> origin/ahmed
      .subscribe({
        next: (res) => {
          console.log('Backend response:', res);

<<<<<<< HEAD

=======
>>>>>>> origin/ahmed
          this.isLoading = false;
          this.signupForm.reset({ role: 'USER' });

          // 👉 open popup
          this.showSuccessPopup = true;
          this.cd.detectChanges();
<<<<<<< HEAD

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
=======
        },

        error: (err) => {
          console.error('Signup failed', err); // <-- already logging errors
>>>>>>> origin/ahmed
          this.isLoading = false;
        }
      });


  }
  onPhoneInput(event: any) {
    let value = event.target.value;

    // Always keep +216 prefix
    if (!value.startsWith('+216')) {
      value = '+216' + value.replace(/\D/g, '');
    }

    // Keep only digits after +216 and max 8 digits
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