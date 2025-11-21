import {
  Component,
  inject,
  input,
  output,
  OnInit,
  effect,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

export interface FormField {
  name: string;
  label: string;
  type: 'text' | 'textarea' | 'select' | 'datetime-local' | 'date' | 'time' | 'number';
  placeholder?: string;
  options?: { label: string; value: any }[];
  validators?: any[];
  fullWidth?: boolean;
}

@Component({
  selector: 'app-generic-form',
  imports: [
    ReactiveFormsModule, 
    CommonModule, 
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './generic-form.component.html',
  standalone: true,
})
export class GenericFormComponent implements OnInit {
  fields = input<FormField[]>([]);
  formTitle = input<string>('Form');
  submitText = input<string>('Submit');
  initialData = input<any>({});
  readonly = input<boolean>(false);
  disabledFields = input<string[]>([]);
  loading = input<boolean>(false);
  icon = input<{ name: string; size?: number; class?: string } | undefined>(
    undefined
  );

  formSubmit = output<any>();

  formGroup!: FormGroup;
  private fb = inject(FormBuilder);

  private isInitialized = false;

  constructor() {
    // Reaccionar a cambios en los fields para reconstruir el formulario
    // Solo después de la inicialización inicial
    effect(() => {
      const currentFields = this.fields();
      if (this.isInitialized && currentFields.length > 0) {
        this.buildForm();
      }
    });

    // Reaccionar a cambios en initialData (solo después de la inicialización)
    effect(() => {
      const currentData = this.initialData();
      if (this.isInitialized && this.formGroup && Object.keys(currentData).length > 0) {
        this.applyInitialData();
      }
    });

    // Reaccionar a cambios en readonly
    effect(() => {
      if (this.isInitialized && this.formGroup) {
        this.updateReadonlyState();
      }
    });

    // Reaccionar a cambios en disabledFields
    effect(() => {
      if (this.isInitialized && this.formGroup) {
        this.updateDisabledFields();
      }
    });
  }

  ngOnInit() {
    this.buildForm();
    this.isInitialized = true;
  }

  private buildForm() {
    const group: any = {};
    this.fields().forEach((field) => {
      // Asegurar que los validators sean un array válido
      let validators = field.validators && Array.isArray(field.validators) 
        ? [...field.validators]
        : [];
      
      // Agregar validadores automáticos según el tipo de campo
      if (field.type === 'date') {
        validators.push(this.minDateValidator());
      }
      if (field.type === 'time') {
        validators.push(this.timeRangeValidator());
      }
      if (field.type === 'datetime-local') {
        validators.push(this.minDateValidator());
        validators.push(this.dateTimeRangeValidator());
      }
      
      group[field.name] = ['', validators];
    });
    this.formGroup = this.fb.group(group);

    this.applyInitialData();
    this.updateReadonlyState();
    this.updateDisabledFields();
  }

  private applyInitialData() {
    if (this.formGroup && Object.keys(this.initialData()).length > 0) {
      this.formGroup.patchValue(this.initialData());
    }
  }

  private updateReadonlyState() {
    if (!this.formGroup) return;

    if (this.readonly()) {
      this.formGroup.disable();
    } else {
      this.formGroup.enable();
      this.updateDisabledFields();
    }
  }

  private updateDisabledFields() {
    if (!this.formGroup || this.readonly()) return;

    this.disabledFields().forEach((fieldName) => {
      const control = this.formGroup.get(fieldName);
      if (control) {
        control.disable();
      }
    });
  }

  onSubmit() {
    if (this.readonly()) return;

    if (this.formGroup.valid) {
      // Incluir también los campos deshabilitados en el valor
      const formValue = {
        ...this.formGroup.getRawValue(),
      };
      this.formSubmit.emit(formValue);
    } else {
      this.formGroup.markAllAsTouched();
    }
  }

  isFieldDisabled(fieldName: string): boolean {
    return this.readonly() || this.disabledFields().includes(fieldName);
  }

  getDisplayValue(fieldName: string): string {
    if (!this.formGroup) return 'No especificado';
    
    const control = this.formGroup.get(fieldName);
    if (!control) return 'No especificado';
    
    const value = control.value;

    if (value === null || value === undefined || value === '') {
      return 'No especificado';
    }

    const field = this.fields().find((f) => f.name === fieldName);

    // Para fechas (date), formatear
    if (field?.type === 'date' && value) {
      const date = new Date(value);
      return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      });
    }

    // Para fechas con hora (datetime-local), formatear
    if (field?.type === 'datetime-local' && value) {
      try {
        const date = new Date(value);
        if (!isNaN(date.getTime())) {
          return date.toLocaleDateString('es-ES', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
          });
        }
      } catch (e) {
        return String(value);
      }
    }

    // Para selects, mostrar el label
    if (field?.type === 'select' && field.options) {
      const option = field.options.find((opt) => opt.value === value);
      return option ? option.label : String(value);
    }

    // Para números, formatear si es necesario
    if (field?.type === 'number' && value !== null && value !== undefined) {
      return String(value);
    }

    return String(value);
  }

  // Método auxiliar para obtener el estado de validación de un campo
  isFieldInvalid(fieldName: string): boolean {
    if (!this.formGroup) return false;
    const control = this.formGroup.get(fieldName);
    return control ? (control.invalid && control.touched) : false;
  }

  // Método auxiliar para obtener el mensaje de error
  getFieldError(fieldName: string): string {
    if (!this.formGroup) return '';
    const control = this.formGroup.get(fieldName);
    if (!control || !control.errors || !control.touched) return '';

    if (control.errors['required']) {
      return 'Este campo es requerido';
    }
    if (control.errors['email']) {
      return 'Debe ser un email válido';
    }
    if (control.errors['minDate']) {
      return control.errors['minDate'].message || 'No se pueden seleccionar fechas pasadas';
    }
    if (control.errors['timeRange']) {
      return control.errors['timeRange'].message || 'El horario debe estar entre 7:00 y 22:00';
    }
    if (control.errors['min']) {
      return `El valor mínimo es ${control.errors['min'].min}`;
    }
    if (control.errors['max']) {
      return `El valor máximo es ${control.errors['max'].max}`;
    }
    if (control.errors['minlength']) {
      return `Mínimo ${control.errors['minlength'].requiredLength} caracteres`;
    }
    if (control.errors['maxlength']) {
      return `Máximo ${control.errors['maxlength'].requiredLength} caracteres`;
    }
    if (control.errors['pattern']) {
      return 'El formato no es válido';
    }

    return 'Campo inválido';
  }

  // Validador para fecha mínima (hoy)
  private minDateValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      
      const inputDate = new Date(control.value);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      if (inputDate < today) {
        return { minDate: { value: control.value, message: 'No se pueden seleccionar fechas pasadas' } };
      }
      
      return null;
    };
  }

  // Validador para rango de hora (7:00 - 22:00)
  private timeRangeValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      
      const timeValue = control.value;
      const [hours, minutes] = timeValue.split(':').map(Number);
      
      if (hours < 7 || hours > 22 || (hours === 22 && minutes > 0)) {
        return { timeRange: { value: control.value, message: 'El horario debe estar entre 7:00 y 22:00' } };
      }
      
      return null;
    };
  }

  // Validador para datetime-local (fecha mínima y rango de hora)
  private dateTimeRangeValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      
      const dateTime = new Date(control.value);
      const hours = dateTime.getHours();
      const minutes = dateTime.getMinutes();
      
      if (hours < 7 || hours > 22 || (hours === 22 && minutes > 0)) {
        return { timeRange: { value: control.value, message: 'El horario debe estar entre 7:00 y 22:00' } };
      }
      
      return null;
    };
  }

  // Obtener fecha mínima (hoy) en formato YYYY-MM-DD
  getMinDate(): string {
    const today = new Date();
    return today.toISOString().split('T')[0];
  }

  // Obtener hora mínima
  getMinTime(): string {
    return '07:00';
  }

  // Obtener hora máxima
  getMaxTime(): string {
    return '22:00';
  }

  // Método auxiliar para abrir el selector nativo del input
  openPicker(inputElement: any): void {
    if (inputElement && typeof inputElement.showPicker === 'function') {
      try {
        inputElement.showPicker();
      } catch (error) {
        // Fallback: hacer focus y click en el input
        inputElement.focus();
        inputElement.click();
      }
    } else {
      // Fallback para navegadores que no soportan showPicker()
      inputElement.focus();
      inputElement.click();
    }
  }
}
