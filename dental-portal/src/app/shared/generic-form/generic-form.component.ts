import {
  Component,
  inject,
  input,
  output,
  OnInit,
  OnChanges,
  SimpleChanges,
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
})
export class GenericFormComponent implements OnInit, OnChanges {
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

  ngOnInit() {
    this.buildForm();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['fields'] && !changes['fields'].firstChange) {
      this.buildForm();
    }

    if (changes['initialData'] && !changes['initialData'].firstChange) {
      this.applyInitialData();
    }

    if (changes['readonly']) {
      this.updateReadonlyState();
    }

    if (changes['disabledFields']) {
      this.updateDisabledFields();
    }
  }

  private buildForm() {
    const group: any = {};
    this.fields().forEach((field) => {
      group[field.name] = ['', field.validators || []];
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
    const value = this.formGroup?.get(fieldName)?.value;

    if (!value) return 'No especificado';

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
      const date = new Date(value);
      return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      });
    }

    // Para selects, mostrar el label
    if (field?.type === 'select' && field.options) {
      const option = field.options.find((opt) => opt.value === value);
      return option ? option.label : value;
    }

    return value;
  }
}
