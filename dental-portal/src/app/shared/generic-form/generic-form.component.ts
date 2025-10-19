import { Component, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { FormGroup } from '@angular/forms';

interface FormField {
  name: string;
  label: string;
  type: 'text' | 'textarea' | 'select' | 'datetime-local' | 'number';
  placeholder?: string;
  options?: { label: string; value: any }[];
  validators?: any[];
}

@Component({
  selector: 'app-generic-form',
  imports: [ReactiveFormsModule],
  templateUrl: './generic-form.component.html',
})
export class GenericFormComponent {
  fields = input<FormField[]>([]);
  formTitle = input<string>('Form');
  submitText = input<string>('Submit');

  formSubmit = output<any>();

  formGroup!: FormGroup;
  private fb = inject(FormBuilder);

  ngOnInit() {
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