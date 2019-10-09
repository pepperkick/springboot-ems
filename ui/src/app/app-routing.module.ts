import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent} from './home/home.component';
import { EmployeeDetailComponent } from './employee-detail/employee-detail.component';
import { EmployeeAddComponent } from './employee-add/employee-add.component';

const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'add', component: EmployeeAddComponent },
  { path: 'employees/:id', component: EmployeeDetailComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    onSameUrlNavigation: 'reload'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
