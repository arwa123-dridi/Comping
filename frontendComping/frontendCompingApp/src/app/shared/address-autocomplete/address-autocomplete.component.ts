import { Component, ElementRef, Input, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Subject, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { NominatimService, NominatimSuggestion } from '../../services/nominatim.service';

@Component({
  selector: 'app-address-autocomplete',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './address-autocomplete.component.html',
  styleUrls: ['./address-autocomplete.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => AddressAutocompleteComponent),
      multi: true
    }
  ]
})
export class AddressAutocompleteComponent implements ControlValueAccessor {
  @Input() placeholder = '';
  @Input() countryCode = 'tn';

  value = '';
  suggestions: NominatimSuggestion[] = [];
  showSuggestions = false;
  loading = false;
  disabled = false;

  private searchSubject = new Subject<string>();
  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private nominatimService: NominatimService, private el: ElementRef) {
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(query => {
        if (!query || query.trim().length < 3) {
          this.loading = false;
          return of([] as NominatimSuggestion[]);
        }
        this.loading = true;
        return this.nominatimService.search(query, this.countryCode).pipe(
          catchError(() => of([] as NominatimSuggestion[]))
        );
      })
    ).subscribe(results => {
      this.suggestions = results;
      this.showSuggestions = results.length > 0;
      this.loading = false;
    });
  }

  onInput(newValue: string): void {
    this.value = newValue;
    this.onChange(newValue);
    this.searchSubject.next(newValue);
  }

  selectSuggestion(s: NominatimSuggestion): void {
    this.value = s.display_name;
    this.onChange(this.value);
    this.suggestions = [];
    this.showSuggestions = false;
  }

  onFocusInput(): void {
    this.showSuggestions = this.suggestions.length > 0;
  }

  onBlurInput(): void {
    // Laisse le (mousedown) de la suggestion se déclencher avant de fermer la liste.
    setTimeout(() => { this.showSuggestions = false; }, 150);
    this.onTouched();
  }

  writeValue(value: string): void {
    this.value = value ?? '';
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}
