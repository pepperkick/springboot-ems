import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../api.service';
import { Designation } from '../designation';
import { Employee } from '../employee';

@Component({
  selector: 'app-employee-add',
  templateUrl: './employee-add.component.html',
  styleUrls: ['./employee-add.component.less']
})
export class EmployeeAddComponent implements OnInit {
  designations: Designation[];
  employees: Employee[];
  employeeInfo = {
    name: '',
    designation: '',
    manager: -1
  };
  designationInfo = {
    name: '',
    higher: -1,
    equals: false
  };

  constructor(
    private api: ApiService,
    private snackBar: MatSnackBar,
    private router: Router) {}

  async ngOnInit() {
    this.designations = await this.api.getDesignations();
    this.employees = await this.api.getEmployees();
  }

  async addEmployee() {
    try {
      if (!this.employeeInfo.name) {
        return this.snackBar.open('Name cannot be empty', '', {
          duration: 2000
        });
      } else if (!this.employeeInfo.designation) {
        return this.snackBar.open('Designation cannot be empty', '', {
          duration: 2000
        });
      }

      const employee = {
        name: this.employeeInfo.name,
        jobTitle: this.employeeInfo.designation,
        managerId: this.employeeInfo.manager
      };

      const emp: Employee = await this.api.addEmployee(employee);

      await this.router.navigate([ 'employee', emp.id ]);
    } catch (error) {
      this.snackBar.open(error.error, '', {
        duration: 2000
      });

      console.log(error);
    }
  }

  async addDesignation() {
    try {
      if (!this.designationInfo.name) {
        return this.snackBar.open('Name cannot be empty', '', {
          duration: 2000
        });
      }

      const designation = {
        name: this.designationInfo.name,
        higher: this.designationInfo.higher,
        equals: this.designationInfo.equals
      };

      const desg: Designation = await this.api.addDesignation(designation);

      await this.router.navigate([ 'home' ]);
    } catch (error) {
      this.snackBar.open(error.error, '', {
        duration: 2000
      });

      console.log(error);
    }
  }
}
