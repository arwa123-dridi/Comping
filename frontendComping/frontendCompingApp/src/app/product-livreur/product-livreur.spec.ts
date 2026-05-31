import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductLivreur } from './product-livreur';
import { CommandeService } from '../services/CommandeService';
import { ToastrService } from 'ngx-toastr';
import { of, throwError } from 'rxjs';

describe('ProductLivreur', () => {
  let component: ProductLivreur;
  let fixture: ComponentFixture<ProductLivreur>;

  let commandeService: jasmine.SpyObj<CommandeService>;
  let toastr: jasmine.SpyObj<ToastrService>;

  beforeEach(async () => {

    const commandeServiceMock = jasmine.createSpyObj(
      'CommandeService',
      ['getCommandesByLivreur', 'markLivree']
    );

    const toastrMock = jasmine.createSpyObj(
      'ToastrService',
      ['success', 'error', 'warning']
    );

    await TestBed.configureTestingModule({
      imports: [ProductLivreur],
      providers: [
        { provide: CommandeService, useValue: commandeServiceMock },
        { provide: ToastrService, useValue: toastrMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductLivreur);
    component = fixture.componentInstance;

    commandeService = TestBed.inject(
      CommandeService
    ) as jasmine.SpyObj<CommandeService>;

    toastr = TestBed.inject(
      ToastrService
    ) as jasmine.SpyObj<ToastrService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return empty string when no token exists', () => {
    spyOn(localStorage, 'getItem').and.returnValue(null);

    const result = component.getLivreurIdFromToken();

    expect(result).toBe('');
  });

  it('should extract livreur id from token', () => {

    const payload = {
      id: 'LIV1'
    };

    const token =
      'header.' +
      btoa(JSON.stringify(payload)) +
      '.signature';

    spyOn(localStorage, 'getItem').and.returnValue(token);

    const result = component.getLivreurIdFromToken();

    expect(result).toBe('LIV1');
  });

  it('should load commandes successfully', () => {

    const commandesMock: any[] = [
      {
        id: '1',
        totalCommande: 100
      }
    ];

    component.livreurId = 'LIV1';

    commandeService.getCommandesByLivreur
      .and.returnValue(of(commandesMock));

    component.loadCommandes();

    expect(
      commandeService.getCommandesByLivreur
    ).toHaveBeenCalledWith('LIV1');

    expect(component.commandes.length).toBe(1);
  });

  it('should show warning if livreur id is missing', () => {

    component.livreurId = '';

    component.loadCommandes();

    expect(toastr.warning).toHaveBeenCalled();
  });

  it('should show error when loading commandes fails', () => {

    component.livreurId = 'LIV1';

    commandeService.getCommandesByLivreur.and.returnValue(
      throwError(() => new Error('Backend error'))
    );

    component.loadCommandes();

    expect(toastr.error).toHaveBeenCalled();
  });

  it('should mark commande as delivered', () => {

    component.livreurId = 'LIV1';

    spyOn(component, 'loadCommandes');

    commandeService.markLivree.and.returnValue(of({}));

    component.markAsLivree('CMD1');

    expect(commandeService.markLivree)
      .toHaveBeenCalledWith('CMD1', 'LIV1');

    expect(toastr.success).toHaveBeenCalled();

    expect(component.loadCommandes)
      .toHaveBeenCalled();
  });

  it('should show error when markLivree fails', () => {

    component.livreurId = 'LIV1';

    commandeService.markLivree.and.returnValue(
      throwError(() => new Error('Update failed'))
    );

    component.markAsLivree('CMD1');

    expect(toastr.error).toHaveBeenCalled();
  });

  it('should call loadCommandes on ngOnInit', () => {

    spyOn(component, 'getLivreurIdFromToken')
      .and.returnValue('LIV1');

    spyOn(component, 'loadCommandes');

    component.ngOnInit();

    expect(component.livreurId).toBe('LIV1');

    expect(component.loadCommandes)
      .toHaveBeenCalled();
  });

});