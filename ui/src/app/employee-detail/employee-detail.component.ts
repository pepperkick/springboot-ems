import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute, RouterEvent, NavigationEnd} from '@angular/router';
import { Employee } from '../employee';
import { ApiService } from '../api.service';
import { filter } from 'rxjs/operators';


@Component({
  selector: 'app-employee-detail',
  templateUrl: './employee-detail.component.html',
  styleUrls: ['./employee-detail.component.less']
})
export class EmployeeDetailComponent implements OnInit {
  private employee: Employee;
  private readonly displayedColumns: string[] = [ 'name', 'jobTitle', 'options' ];

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private router: Router) {}

  async ngOnInit() {
    this.router.events.pipe(filter((event: RouterEvent) => event instanceof NavigationEnd))
      .subscribe(() => this.fetch());

    this.fetch();
  }

  async fetch() {
    this.employee = await this.api.getEmployee(parseInt(this.route.snapshot.paramMap.get('id'), 10));
  }
}
