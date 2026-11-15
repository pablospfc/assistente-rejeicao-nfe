import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [App], providers: [provideRouter([])] }).compileComponents();
  });

  it('mostra o nome da aplicação e o aviso acadêmico', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const texto = (fixture.nativeElement as HTMLElement).textContent;

    expect(texto).toContain('Assistente de Rejeições da NF-e');
    expect(texto).toContain('Não substitui a validação da SEFAZ.');
  });
});
