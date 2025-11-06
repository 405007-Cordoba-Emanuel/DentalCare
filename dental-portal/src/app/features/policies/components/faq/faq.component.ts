import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface FAQItem {
  question: string;
  answer: string;
  category: string;
}

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './faq.component.html',
  styleUrls: ['./faq.component.css']
})
export class FaqComponent {
  selectedCategory = 'general';
  openItems: Set<number> = new Set();

  faqs: FAQItem[] = [
    // General
    {
      question: '¿Qué es DentalCare?',
      answer: 'DentalCare es una plataforma integral de gestión dental que conecta pacientes con profesionales odontológicos, facilitando la programación de citas, gestión de historiales médicos y comunicación entre profesionales y pacientes.',
      category: 'general'
    },
    {
      question: '¿Cómo me registro en la plataforma?',
      answer: 'Puede registrarse como paciente o como odontólogo profesional. Haga clic en "Registrarse" en la página principal, complete el formulario con sus datos y verifique su cuenta a través del correo electrónico que recibirá.',
      category: 'general'
    },
    {
      question: '¿La plataforma es gratuita?',
      answer: 'Para los pacientes, el uso básico es gratuito. Los odontólogos profesionales pueden elegir entre planes gratuitos limitados o suscripciones premium con funciones avanzadas.',
      category: 'general'
    },
    
    // Pacientes
    {
      question: '¿Cómo puedo programar una cita?',
      answer: 'Una vez registrado, puede buscar odontólogos en su área, ver sus perfiles y horarios disponibles, y programar una cita directamente desde la plataforma. Recibirá confirmación por correo electrónico.',
      category: 'pacientes'
    },
    {
      question: '¿Puedo cancelar o reprogramar mi cita?',
      answer: 'Sí, puede cancelar o reprogramar su cita hasta 24 horas antes del horario programado. Para hacerlo, acceda a su panel de usuario y seleccione la cita que desea modificar.',
      category: 'pacientes'
    },
    {
      question: '¿Cómo veo mi historial dental?',
      answer: 'Su historial dental está disponible en su panel de usuario. Puede ver tratamientos realizados, próximas citas, radiografías (cuando estén disponibles) y notas de sus odontólogos.',
      category: 'pacientes'
    },
    {
      question: '¿Mis datos están seguros?',
      answer: 'Absolutamente. Implementamos medidas de seguridad de nivel bancario para proteger su información personal y médica. Todos los datos están encriptados y solo usted y sus odontólogos autorizados pueden acceder a su información.',
      category: 'pacientes'
    },
    
    // Odontólogos
    {
      question: '¿Cómo me registro como odontólogo?',
      answer: 'Los odontólogos deben completar un proceso de verificación que incluye la validación de su matrícula profesional, documentos de identidad y certificaciones. Este proceso puede tomar 2-3 días hábiles.',
      category: 'odontologos'
    },
    {
      question: '¿Puedo gestionar múltiples consultorios?',
      answer: 'Sí, con una cuenta profesional puede gestionar múltiples ubicaciones, cada una con su propio calendario, horarios y equipo de trabajo.',
      category: 'odontologos'
    },
    {
      question: '¿Cómo gestiono mi calendario?',
      answer: 'Puede configurar sus horarios de trabajo, días de descanso, tipos de citas y duraciones. La plataforma también le permite bloquear horarios específicos y establecer recordatorios automáticos.',
      category: 'odontologos'
    },
    {
      question: '¿Puedo acceder a los historiales de mis pacientes?',
      answer: 'Sí, una vez que un paciente autoriza el acceso, puede ver su historial completo, incluyendo tratamientos previos, alergias, medicamentos y notas de otros profesionales.',
      category: 'odontologos'
    },
    
    // Técnico
    {
      question: '¿Qué navegadores son compatibles?',
      answer: 'DentalCare es compatible con todos los navegadores modernos, incluyendo Chrome, Firefox, Safari, Edge y Opera. Recomendamos usar la versión más reciente para la mejor experiencia.',
      category: 'tecnico'
    },
    {
      question: '¿Hay una aplicación móvil?',
      answer: 'Actualmente estamos desarrollando aplicaciones móviles para iOS y Android. Mientras tanto, puede acceder a la plataforma desde su dispositivo móvil a través del navegador web.',
      category: 'tecnico'
    },
    {
      question: '¿Qué hago si no puedo acceder a mi cuenta?',
      answer: 'Use la función "¿Olvidó su contraseña?" en la página de inicio de sesión. Si el problema persiste, contacte a nuestro equipo de soporte técnico.',
      category: 'tecnico'
    },
    {
      question: '¿Cómo contacto al soporte técnico?',
      answer: 'Puede contactarnos por correo electrónico a soporte@dentalcarepro.com, por teléfono al +54 11 1234-5678, o a través del chat en vivo disponible en la plataforma.',
      category: 'tecnico'
    }
  ];

  get filteredFaqs(): FAQItem[] {
    return this.faqs.filter(faq => faq.category === this.selectedCategory);
  }

  toggleItem(index: number): void {
    if (this.openItems.has(index)) {
      this.openItems.delete(index);
    } else {
      this.openItems.add(index);
    }
  }

  isOpen(index: number): boolean {
    return this.openItems.has(index);
  }

  setCategory(category: string): void {
    this.selectedCategory = category;
    this.openItems.clear();
  }
}
