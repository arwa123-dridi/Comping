import { Component } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ProductListComponent } from './product-list.component';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';

import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';

/* ============================
   MOCK CHILD COMPONENTS
============================ */

@Component({
  selector: 'app-sidebar',
  standalone: true,
  template: ''
})
class MockSidebarComponent { }

@Component({
  selector: 'app-add-product',
  standalone: true,
  template: ''
})
class MockAddProductComponent { }

@Component({
  selector: 'app-edit-product',
  standalone: true,
  template: ''
})
class MockEditProductComponent { }

describe('ProductListComponent', () => {

  let component: ProductListComponent;
  let fixture: ComponentFixture<ProductListComponent>;
  let httpMock: HttpTestingController;

  const toastrSpy = jasmine.createSpyObj('ToastrService', [
    'success',
    'error',
    'warning'
  ]);

  const routerSpy = jasmine.createSpyObj('Router', [
    'navigate'
  ]);

  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [
        ProductListComponent,
        HttpClientTestingModule   // ✅ single source of truth for HttpClient
      ],
      providers: [
        { provide: ToastrService, useValue: toastrSpy },
        { provide: Router, useValue: routerSpy }
      ]
    })
      .overrideComponent(ProductListComponent, {
        set: {
          imports: [
            CommonModule,
            FormsModule,
            // ✅ NO HttpClientModule here — causes a second HttpClient
            //    instance that bypasses HttpClientTestingModule entirely
            MockSidebarComponent,
            MockAddProductComponent,
            MockEditProductComponent
          ]
        }
      })
      .compileComponents();

    fixture = TestBed.createComponent(ProductListComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);

    // ✅ Stub loadProducts BEFORE detectChanges so ngOnInit makes no HTTP call
    spyOn(component, 'loadProducts').and.callFake(() => {});

    fixture.detectChanges();
  });

  afterEach(() => {
    // ✅ Ensures no unexpected requests were made
    httpMock.verify();
  });

  /* ============================
      CREATE
  ============================ */

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /* ============================
      LOAD PRODUCTS
  ============================ */

  it('should call loadProducts on init', () => {
    component.ngOnInit();
    expect(component.loadProducts).toHaveBeenCalled();
  });

  /* ============================
      FILTER
  ============================ */

  it('should filter products by category', () => {

    component.products = [
      {
        id: '1',
        nomProduit: 'Tent',
        descriptionProduit: '',
        prixProduit: 100,
        categorieProduit: 'TENTES',
        statut: 'ACTIVE'
      },
      {
        id: '2',
        nomProduit: 'Bag',
        descriptionProduit: '',
        prixProduit: 50,
        categorieProduit: 'SACS_A_DOS',
        statut: 'ACTIVE'
      }
    ];

    component.selectedCategory = 'TENTES';
    component.applyFilters();

    expect(component.filteredProducts.length).toBe(1);
    expect(component.filteredProducts[0].nomProduit).toBe('Tent');
  });

  /* ============================
      DELETE MODAL
  ============================ */

  it('should open delete modal', () => {

    component.confirmDelete('123');

    expect(component.showDeleteModal).toBeTrue();
    expect(component.selectedDeleteId).toBe('123');
  });

  it('should cancel delete modal', () => {

    component.showDeleteModal = true;
    component.selectedDeleteId = '123';

    component.cancelDelete();

    expect(component.showDeleteModal).toBeFalse();
    expect(component.selectedDeleteId).toBeNull();
  });

  /* ============================
      NAVIGATION
  ============================ */

  it('should navigate to product details', () => {

    component.viewProduct('10');

    expect(routerSpy.navigate)
      .toHaveBeenCalledWith(['/products', '10']);
  });

  /* ============================
      PROMOTION
  ============================ */

  it('should return true when promotion is active', () => {

    const now = new Date();

    const product = {
      promoPrice: 50,
      promoStart: new Date(now.getTime() - 10000).toISOString(),
      promoEnd: new Date(now.getTime() + 10000).toISOString()
    };

    expect(component.isPromotionActive(product)).toBeTrue();
  });

  it('should return false when promotion expired', () => {

    const now = new Date();

    const product = {
      promoPrice: 50,
      promoStart: new Date(now.getTime() - 20000).toISOString(),
      promoEnd: new Date(now.getTime() - 10000).toISOString()
    };

    expect(component.isPromotionActive(product)).toBeFalse();
  });

  /* ============================
      DELETE PRODUCT
  ============================ */

  it('should delete product successfully', fakeAsync(() => {

    // ✅ Restore real implementation so deleteConfirmed actually fires HTTP
    (component.loadProducts as jasmine.Spy).and.callFake(() => {});

    // Reset call tracking
    (component.loadProducts as jasmine.Spy).calls.reset();
    toastrSpy.success.calls.reset();

    // Set the product ID to delete
    component.selectedDeleteId = '1';

    // Trigger the delete
    component.deleteConfirmed();

    // ✅ Intercept the DELETE request via httpMock (same HttpClient instance now)
    const deleteReq = httpMock.expectOne(
      'http://localhost:8087/api/produits/deleteProduct/1'
    );
    expect(deleteReq.request.method).toBe('DELETE');

    // ✅ Flush a text response (matches responseType: 'text')
    deleteReq.flush('deleted');

    tick();

    // ✅ Assert success toast shown
    expect(toastrSpy.success).toHaveBeenCalled();

    // ✅ Assert loadProducts called to refresh list
    expect(component.loadProducts).toHaveBeenCalled();

    // ✅ Assert modal closed by cancelDelete()
    expect(component.showDeleteModal).toBeFalse();
    expect(component.selectedDeleteId).toBeNull();
  }));

});