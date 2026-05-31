import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SigninComponent } from './signin.component';
import { SigninService } from '../services/signin.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

describe('SigninComponent', () => {
  let component: SigninComponent;
  let fixture: ComponentFixture<SigninComponent>;
  let signinServiceMock: jasmine.SpyObj<SigninService>;
  let router: Router;

  beforeEach(async () => {
    signinServiceMock = jasmine.createSpyObj('SigninService', ['login']);

    await TestBed.configureTestingModule({
      imports: [
        SigninComponent,
        RouterTestingModule  // ⭐ FIX
      ],
      providers: [
        { provide: SigninService, useValue: signinServiceMock }
      ]
    }).compileComponents();

    fixture   = TestBed.createComponent(SigninComponent);
    component = fixture.componentInstance;
    router    = TestBed.inject(Router); // ⭐ inject le vrai router du module de test
    fixture.detectChanges();
  });

  // ================= CREATION =================
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ================= VALEURS INITIALES =================
  it('should have empty fields by default', () => {
    expect(component.email).toBe('');
    expect(component.password).toBe('');
    expect(component.rememberMe).toBeFalse();
    expect(component.showPassword).toBeFalse();
    expect(component.isLoading).toBeFalse();
    expect(component.errorMsg).toBe('');
  });

  // ================= TOGGLE PASSWORD =================
  it('should toggle showPassword', () => {
    expect(component.showPassword).toBeFalse();
    component.togglePassword();
    expect(component.showPassword).toBeTrue();
    component.togglePassword();
    expect(component.showPassword).toBeFalse();
  });

  // ================= VALIDATION CHAMPS VIDES =================
  it('should set errorMsg if email or password is empty', () => {
    component.email    = '';
    component.password = '';
    component.onSubmit();
    expect(component.errorMsg).toBe('Veuillez remplir tous les champs.');
    expect(signinServiceMock.login).not.toHaveBeenCalled();
  });

  it('should set errorMsg if only email is empty', () => {
    component.email    = '';
    component.password = '123456';
    component.onSubmit();
    expect(component.errorMsg).toBe('Veuillez remplir tous les champs.');
  });

  it('should set errorMsg if only password is empty', () => {
    component.email    = 'test@test.com';
    component.password = '';
    component.onSubmit();
    expect(component.errorMsg).toBe('Veuillez remplir tous les champs.');
  });

  // ================= LOGIN ADMIN =================
  it('should redirect to /admin/dashboard if role is ADMIN', () => {
    component.email    = 'admin@test.com';
    component.password = 'admin123';

    signinServiceMock.login.and.returnValue(of({ token: 'fake-token' } as any));
    spyOn(localStorage, 'getItem').and.returnValue('ADMIN');
    const navigateSpy = spyOn(router, 'navigate');

    component.onSubmit();

    expect(navigateSpy).toHaveBeenCalledWith(['/admin/dashboard']);
  });

  it('should redirect to /admin/dashboard if role is ROLE_ADMIN', () => {
    component.email    = 'admin@test.com';
    component.password = 'admin123';

    signinServiceMock.login.and.returnValue(of({ token: 'fake-token' } as any));
    spyOn(localStorage, 'getItem').and.returnValue('ROLE_ADMIN');
    const navigateSpy = spyOn(router, 'navigate');

    component.onSubmit();

    expect(navigateSpy).toHaveBeenCalledWith(['/admin/dashboard']);
  });

  // ================= LOGIN USER =================
  it('should redirect to /Campino if role is USER', () => {
    component.email    = 'user@test.com';
    component.password = 'user123';

    signinServiceMock.login.and.returnValue(of({ token: 'fake-token' } as any));
    spyOn(localStorage, 'getItem').and.returnValue('USER');
    const navigateSpy = spyOn(router, 'navigate');

    component.onSubmit();

    expect(navigateSpy).toHaveBeenCalledWith(['/Campino']);
  });

  // ================= GESTION ERREURS =================
  it('should set errorMsg for 401 error', () => {
    component.email    = 'user@test.com';
    component.password = 'wrongpassword';

    signinServiceMock.login.and.returnValue(
      throwError(() => ({ status: 401, error: { message: 'INVALID_PASSWORD' } }))
    );

    component.onSubmit();

    expect(component.errorMsg).toBe('Email ou mot de passe incorrect.');
    expect(component.isLoading).toBeFalse();
  });

  it('should set errorMsg for 403 error (compte désactivé)', () => {
    component.email    = 'disabled@test.com';
    component.password = 'password123';

    signinServiceMock.login.and.returnValue(
      throwError(() => ({ status: 403, error: { message: 'ACCOUNT_DISABLED' } }))
    );

    component.onSubmit();

    expect(component.errorMsg).toBe('Votre compte est désactivé');
    expect(component.isLoading).toBeFalse();
  });

  it('should set errorMsg for status 0 (serveur inaccessible)', () => {
    component.email    = 'user@test.com';
    component.password = 'password123';

    signinServiceMock.login.and.returnValue(
      throwError(() => ({ status: 0, error: {} }))
    );

    component.onSubmit();

    expect(component.errorMsg).toBe('Serveur inaccessible (port 8087).');
  });

  it('should set errorMsg générique pour une erreur inconnue', () => {
    component.email    = 'user@test.com';
    component.password = 'password123';

    signinServiceMock.login.and.returnValue(
      throwError(() => ({ status: 500, error: { message: 'Erreur interne' } }))
    );

    component.onSubmit();

    expect(component.errorMsg).toBe('Erreur interne');
  });

  // ================= LOADING STATE =================
  it('should set isLoading to false after response', () => {
    component.email    = 'user@test.com';
    component.password = 'password123';

    signinServiceMock.login.and.returnValue(of({ token: 'fake-token' } as any));
    spyOn(localStorage, 'getItem').and.returnValue('USER');
    spyOn(router, 'navigate');

    component.onSubmit();

    expect(component.isLoading).toBeFalse();
  });
});