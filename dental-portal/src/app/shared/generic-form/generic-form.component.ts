import {
  Component,
  inject,
  input,
  output,
  OnInit,
  effect,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';

export interface FormField {
  name: string;
  label: string;
  type: 'text' | 'textarea' | 'select' | 'datetime-local' | 'date' | 'number';
  placeholder?: string;
  options?: { label: string; value: any }[];
  validators?: any[];
  fullWidth?: boolean;
}

@Component({
  selector: 'app-generic-form',
  imports: [ReactiveFormsModule, CommonModule],
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
      const validators = field.validators && Array.isArray(field.validators) 
        ? field.validators 
        : [];
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
}
