import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule, NgIf } from '@angular/common';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-signup',
  //standalone: true,
  imports: [ReactiveFormsModule, HttpClientModule, CommonModule, NgIf],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements AfterViewInit {
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
    'USER',
    'LIVREUR'
  ];


  constructor(private fb: FormBuilder, private http: HttpClient, private cd: ChangeDetectorRef) {

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
      ...formValue,
      telephone: formValue.telephone || null,
      address: formValue.address || null
    };

    console.log('Signup data being sent to backend:', signupData); // <-- log cleaned data

    this.http.post('http://localhost:8087/api/auth/registerUser', signupData)
      .subscribe({
        next: (res) => {
          console.log('Backend response:', res);

          this.isLoading = false;
          this.signupForm.reset({ role: 'USER' });

          // 👉 open popup
          this.showSuccessPopup = true;
          this.cd.detectChanges();
        },

        error: (err) => {
          console.error('Signup failed', err); // <-- already logging errors
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