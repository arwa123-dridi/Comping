import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductLivreur } from './product-livreur';

describe('ProductLivreur', () => {
  let component: ProductLivreur;
  let fixture: ComponentFixture<ProductLivreur>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductLivreur]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductLivreur);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
