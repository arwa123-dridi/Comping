import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SignupComponent } from './signup.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { By } from '@angular/platform-browser';
import { AuthUserService } from '../services/signup.service';

describe('SignupComponent', () => {
  let component: SignupComponent;
  let fixture: ComponentFixture<SignupComponent>;
  let authService: jasmine.SpyObj<AuthUserService>;

  beforeEach(async () => {

    const authServiceMock = jasmine.createSpyObj('AuthUserService', [
      // ONLY include real methods if used in component
      'getLivreurs'
    ]);

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        HttpClientTestingModule,
        SignupComponent
      ],
      providers: [
        { provide: AuthUserService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SignupComponent);
    component = fixture.componentInstance;

    authService = TestBed.inject(AuthUserService) as jasmine.SpyObj<AuthUserService>;

    fixture.detectChanges();
  });

  // ================= BASIC TEST =================
  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  // ================= FORM INIT =================
  it('should initialize form', () => {
    expect(component.signupForm).toBeDefined();
  });

  // ================= INVALID FORM =================
  it('should be invalid when empty', () => {
    component.signupForm.setValue({
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      confirmPassword: '',
      telephone: '',
      address: '',
      role: null
    });

    expect(component.signupForm.valid).toBeFalse();
  });

  // ================= VALID FORM =================
  it('should be valid when filled', () => {
    component.signupForm.setValue({
      firstName: 'Fatma',
      lastName: 'Barrani',
      email: 'test@gmail.com',
      password: '123456',
      confirmPassword: '123456',
      telephone: '+21612345678',
      address: 'Tunis',
      role: 'USER'
    });

    expect(component.signupForm.valid).toBeTrue();
  });

  // ================= SUBMIT BUTTON TEST =================
  it('should call onSubmit when button clicked', () => {
    spyOn(component, 'onSubmit');

    fixture.detectChanges();

    const button = fixture.debugElement.query(By.css('button'));

    button.nativeElement.click();

    expect(component.onSubmit).toHaveBeenCalled();
  });

});