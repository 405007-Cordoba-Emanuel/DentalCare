import { Component, inject, input, output, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { FormGroup } from '@angular/forms';

export interface FormField {
  name: string;
  label: string;
  type: 'text' | 'textarea' | 'select' | 'datetime-local' | 'number';
  placeholder?: string;
  options?: { label: string; value: any }[];
  validators?: any[];
  fullWidth?: boolean;
}

@Component({
  selector: 'app-generic-form',
  imports: [ReactiveFormsModule],
  templateUrl: './generic-form.component.html',
})
export class GenericFormComponent implements OnInit, OnChanges {
  fields = input<FormField[]>([]);
  formTitle = input<string>('Form');
  submitText = input<string>('Submit');
  // Optional icon: provide { name: string; size?: number; class?: string }
  icon = input<{ name: string; size?: number; class?: string } | undefined>(undefined);

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
  }

  private buildForm() {
    const group: any = {};
    // ✅ Llamamos correctamente a fields() porque es un signal
    this.fields().forEach((field) => {
      group[field.name] = ['', field.validators || []];
    });
    this.formGroup = this.fb.group(group);
  }

  onSubmit() {
    if (this.formGroup.valid) {
      this.formSubmit.emit(this.formGroup.value);
    } else {
      this.formGroup.markAllAsTouched();
    }
  }
}