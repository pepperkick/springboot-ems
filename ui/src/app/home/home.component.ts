import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../api.service';
import { Employee } from '../employee';
import { Designation } from '../designation';
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  private employees: Employee;
  private designations: Designation;
  private readonly displayedColumns: string[] = [ 'name', 'jobTitle', 'manager', 'options' ];
  private readonly designationColumns: string[] = [ 'name', 'level', 'options' ];

  constructor(
    private api: ApiService,
    private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.fetch();
  }

  async fetch() {
    this.employees = await this.api.getEmployees();
    this.designations = await this.api.getDesignations();
  }

  async deleteEmployee(id: number) {
    try {
      await this.api.deleteEmployee(id);
      this.fetch();
      this.snackBar.open('Removed employee successfully', '', {
        duration: 2000
      });
    } catch (error) {
      this.snackBar.open(error.error, '', {
        duration: 2000
      });

      console.log(error);
    }
  }

  async deleteDesignation(id: number) {
    try {
      await this.api.deleteDesignation(id);
      this.fetch();
      this.snackBar.open('Removed designation successfully', '', {
        duration: 2000
      });
    } catch (error) {
      this.snackBar.open(error.error, '', {
        duration: 2000
      });

      console.log(error);
    }
  }
}
