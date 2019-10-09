export class Employee {
  id: number;
  name: string;
  jobTitle: string;
  manager: Employee;
  colleagues: Employee[];
  subordinates: Employee[];
}
