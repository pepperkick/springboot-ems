import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { AppConfig } from './app.config';

@Injectable({
  providedIn: 'root'
})

export class ApiService {
  private readonly apiUrl;

  constructor(private http: HttpClient, private configuration: AppConfig) {
    this.apiUrl = configuration.apiUrl;
  }

  getEmployees<Employee>(): Promise<Employee> {
    return new Promise<Employee>((resolve, reject) => {
      this.http.get<Employee>(`${this.apiUrl}/employees`).subscribe(resolve, reject);
    });
  }

  getEmployee<Employee>(id: number): Promise<Employee> {
    return new Promise<Employee>((resolve, reject) => {
      this.http.get<Employee>(`${this.apiUrl}/employees/${id}`).subscribe(resolve, reject);
    });
  }

  addEmployee<Employee>(body: object): Promise<Employee> {
    return new Promise<Employee>((resolve, reject) => {
      this.http.post<Employee>(`${this.apiUrl}/employees`, body).subscribe(resolve, reject);
    });
  }

  deleteEmployee(id: number): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      this.http.delete<string>(`${this.apiUrl}/employees/${id}`).subscribe(resolve, reject);
    });
  }

  getDesignations<Designation>(): Promise<Designation> {
    return new Promise<Designation>((resolve, reject) => {
      this.http.get<Designation>(`${this.apiUrl}/designations`).subscribe(resolve, reject);
    });
  }

  addDesignation<Designation>(body: object): Promise<Designation> {
    return new Promise<Designation>((resolve, reject) => {
      this.http.post<Designation>(`${this.apiUrl}/designations`, body).subscribe(resolve, reject);
    });
  }

  deleteDesignation(id: number): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      this.http.delete<string>(`${this.apiUrl}/designations/${id}`).subscribe(resolve, reject);
    });
  }
}
