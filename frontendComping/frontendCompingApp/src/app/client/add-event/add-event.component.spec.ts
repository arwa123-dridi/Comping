import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddEventComponent } from './add-event.component';
import { EventService } from '../../services/event.service';
import { ActivityService } from '../../services/activity.service';
import { RecommendationActivity } from '../../services/recommendation-activity';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

describe('AddEventComponent', () => {
  let component: AddEventComponent;
  let fixture: ComponentFixture<AddEventComponent>;
  let eventServiceMock: jasmine.SpyObj<EventService>;
  let activityServiceMock: jasmine.SpyObj<ActivityService>;
  let recommendationServiceMock: jasmine.SpyObj<RecommendationActivity>;
  let router: Router;

  beforeEach(async () => {
    eventServiceMock          = jasmine.createSpyObj('EventService', ['createEvent']);
    activityServiceMock       = jasmine.createSpyObj('ActivityService', ['getAllActivities']);
    recommendationServiceMock = jasmine.createSpyObj('RecommendationActivity', ['suggestActivities']);

    activityServiceMock.getAllActivities.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [AddEventComponent, RouterTestingModule],
      providers: [
        { provide: EventService,          useValue: eventServiceMock },
        { provide: ActivityService,       useValue: activityServiceMock },
        { provide: RecommendationActivity, useValue: recommendationServiceMock }
      ]
    }).compileComponents();

    fixture   = TestBed.createComponent(AddEventComponent);
    component = fixture.componentInstance;
    router    = TestBed.inject(Router);
    fixture.detectChanges();
  });

  // ================= CREATION =================
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ================= VALEURS INITIALES =================
  it('should have default event values', () => {
    expect(component.event.titre).toBe('');
    expect(component.event.prix).toBe(0);
    expect(component.event.capacite).toBe(0);
    expect(component.event.statut).toBe('VALIDE');
    expect(component.event.activityIds).toEqual([]);
    expect(component.selectedActivities).toEqual([]);
    expect(component.showSuggestionPopup).toBeFalse();
    expect(component.showSuccess).toBeFalse();
  });

  // ================= LOAD ACTIVITIES =================
  it('should load activities on init', () => {
    const mockActivities = [
      { idActivity: '1', nom: 'Escalade' },
      { idActivity: '2', nom: 'Natation' }
    ];
    activityServiceMock.getAllActivities.and.returnValue(of(mockActivities));

    component.loadActivities();

    expect(component.activities).toEqual(mockActivities);
  });

  it('should handle error when loading activities', () => {
    activityServiceMock.getAllActivities.and.returnValue(
      throwError(() => new Error('Erreur chargement'))
    );
    const consoleSpy = spyOn(console, 'error');

    component.loadActivities();

    expect(consoleSpy).toHaveBeenCalled();
  });

  // ================= ON ACTIVITY CHANGE =================
  it('should add activity to selectedActivities when checked', () => {
    const mockEvent = { target: { value: 'act1', checked: true } };

    component.onActivityChange(mockEvent);

    expect(component.selectedActivities).toContain('act1');
    expect(component.event.activityIds).toContain('act1');
  });

  it('should remove activity from selectedActivities when unchecked', () => {
    component.selectedActivities = ['act1', 'act2'];

    const mockEvent = { target: { value: 'act1', checked: false } };
    component.onActivityChange(mockEvent);

    expect(component.selectedActivities).not.toContain('act1');
    expect(component.selectedActivities).toContain('act2');
  });

  it('should not add duplicate activity', () => {
    component.selectedActivities = ['act1'];

    const mockEvent = { target: { value: 'act1', checked: true } };
    component.onActivityChange(mockEvent);

    expect(component.selectedActivities.filter(x => x === 'act1').length).toBe(1);
  });

  // ================= ON SUBMIT =================
  it('should call loadSuggestedActivities if no activity selected', () => {
    component.selectedActivities = [];
    component.tagsInput = 'sport, nature';

    const loadSpy = spyOn(component, 'loadSuggestedActivities');

    component.onSubmit();

    expect(loadSpy).toHaveBeenCalled();
    expect(eventServiceMock.createEvent).not.toHaveBeenCalled();
  });

  it('should call createEvent if activities are selected', () => {
    component.selectedActivities = ['act1'];
    component.tagsInput = 'sport';

    eventServiceMock.createEvent.and.returnValue(of({} as any));
    const createSpy = spyOn(component, 'createEvent');

    component.onSubmit();

    expect(createSpy).toHaveBeenCalled();
  });

  it('should parse tags from tagsInput on submit', () => {
    component.selectedActivities = ['act1'];
    component.tagsInput = 'sport, nature, outdoor';

    eventServiceMock.createEvent.and.returnValue(of({} as any));
    spyOn(component, 'createEvent');

    component.onSubmit();

    expect(component.event.tags).toEqual(['sport', 'nature', 'outdoor']);
  });

  // ================= CREATE EVENT =================
  it('should show success and navigate after createEvent', (done) => {
    jasmine.clock().install();

    eventServiceMock.createEvent.and.returnValue(of({} as any));
    const navigateSpy = spyOn(router, 'navigate');

    component.createEvent();

    expect(component.showSuccess).toBeTrue();

    jasmine.clock().tick(2001);

    expect(component.showSuccess).toBeFalse();
    expect(navigateSpy).toHaveBeenCalledWith(['/events/list']);

    jasmine.clock().uninstall();
    done();
  });

  it('should handle error on createEvent', () => {
    eventServiceMock.createEvent.and.returnValue(
      throwError(() => new Error('Erreur création'))
    );
    const consoleSpy = spyOn(console, 'error');

    component.createEvent();

    expect(consoleSpy).toHaveBeenCalled();
  });

  // ================= SUGGESTIONS =================
  it('should load suggested activities and show popup', () => {
    const mockSuggestions = [{ idActivity: '3', nom: 'Yoga' }];
    recommendationServiceMock.suggestActivities.and.returnValue(of(mockSuggestions));

    component.loadSuggestedActivities();

    expect(component.suggestedActivities).toEqual(mockSuggestions);
    expect(component.showSuggestionPopup).toBeTrue();
  });

  it('should handle error on loadSuggestedActivities', () => {
    recommendationServiceMock.suggestActivities.and.returnValue(
      throwError(() => new Error('Erreur suggestion'))
    );
    const consoleSpy = spyOn(console, 'error');

    component.loadSuggestedActivities();

    expect(consoleSpy).toHaveBeenCalled();
  });

  it('should add suggested activity when checked', () => {
    const mockEvent = { target: { value: 'act3', checked: true } };

    component.onSuggestedActivityChange(mockEvent);

    expect(component.selectedSuggestedActivities).toContain('act3');
  });

  it('should remove suggested activity when unchecked', () => {
    component.selectedSuggestedActivities = ['act3', 'act4'];

    const mockEvent = { target: { value: 'act3', checked: false } };
    component.onSuggestedActivityChange(mockEvent);

    expect(component.selectedSuggestedActivities).not.toContain('act3');
  });

  it('should confirm suggested activities and call createEvent', () => {
    component.selectedSuggestedActivities = ['act3', 'act4'];
    const createSpy = spyOn(component, 'createEvent');

    component.confirmSuggestedActivities();

    expect(component.event.activityIds).toEqual(['act3', 'act4']);
    expect(component.showSuggestionPopup).toBeFalse();
    expect(createSpy).toHaveBeenCalled();
  });

  it('should ignore suggestions and call createEvent', () => {
    component.showSuggestionPopup = true;
    const createSpy = spyOn(component, 'createEvent');

    component.ignoreSuggestions();

    expect(component.showSuggestionPopup).toBeFalse();
    expect(createSpy).toHaveBeenCalled();
  });
});