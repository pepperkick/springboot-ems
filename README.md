# EMS

A Spring Boot project for Employee Management System

## Database

#### Designation
- id: Integer (Primary Key)
- level: Integer
- title: String

#### Employee
- id: Integer (Primary Key)
- name: String
- manager: Employee (Reference)
- designation: Designation (Reference)

### Example Data

Designation

| id  | level | title     |
| --- | ----- | --------- |
| 1   | 1     | Director  |
| 2   | 2     | Manager   |
| 3   | 3     | Lead      |
| 4   | 4     | Developer |
| 5   | 4     | DevOps    | 
| 6   | 4     | QA        |
| 7   | 5     | Intern    |

Employee

| id  | name           | manager | designation   |
| --- | -------------- | ------- | ------------- |
| 1   | Thor           | null    | 1 (Director)  |
| 2   | IronMan        | 1       | 2 (Manager)   |
| 3   | Hulk           | 1       | 3 (Lead)      |
| 4   | CaptainAmerica | 1       | 2 (Manager)   |
| 5   | WarMachine     | 2       | 6 (QA)        |
| 6   | Vison          | 2       | 5 (DevOps)    |
| 7   | Falcon         | 4       | 4 (Developer) |
| 8   | AntMan         | 4       | 3 (Lead)      |
| 9   | SpiderMan      | 2       | 7 (Intern)    |
| 10  | BlackWidow     | 3       | 4 (Developer)

## API

#### Error Codes
- 200: OK
- 404: Resource Not Found
- 405: Method Not Allowed
- 406: Not Acceptable

#### GET /employee

Returns list of all employees

Request
```
GET /employee
```

Response
```json
[
  {
    "id": 1,
    "name": "Thor",
    "jobTitle": "Director"
  },
  {
    "id": 2,
    "name": "IronMan",
    "jobTitle": "Manager",
    "manager": {
        "id": 1,
        "name": "Thor",
        "jobTitle": "Director"      
    }   
  } 
  ...
]
```

### POST /employee

Add a new employee

Body
```json
{
  "name": "String Required - Employee Name",
  "jobTitle": "String Required - Employee Designation",
  "managerId": "Integer Optional - Manager Employee ID, Required if current employee is not Director",
}
```

Request
```
POST /employee
body: {
    "name": "DrStrange",
    "jobTitle": "Manager",
    "managerId": 1
}
```

Response

```json
{
    "id": 11,
    "name": "DrStrange",
    "jobTitle": "Manager",
    "manager": {
        "id": 1,
        "name": "Thor",
        "jobTitle": "Director"
    },
    "colleagues": [
        {
            "id": 4,
            "name": "CaptainAmerica",
            "jobTitle": "Manager"
        },
        {
            "id": 2,
            "name": "IronMan",
            "jobTitle": "Manager"
        },
        {
            "id": 3,
            "name": "Hulk",
            "jobTitle": "Lead"
        }
    ]
}
```

### GET employee/{id}

Returns info of specific employee according to ID

Request
```
GET /employee/2
```

Response
```json
{
  "employee": { 
    "id": 2,
    "name": "IronMan",
    "jobTitle": "Manager",
  },
  "manager": {
    "id": 1,
    "name": "Thor",
    "jobTitle": "Director"         
  },
  "colleagues": [
    {
      "id":4,
      "name":"CaptainAmerica",
      "jobTitle":"Manager"
    },
    {
      "id":3,
      "name":"Hulk",
      "jobTitle":"Lead"
    }
  ],
  "subordinates": [
    {
      "id":6,
      "name":"Vison",
      "jobTitle":"DevOps"
    },
    {
      "id":5,
      "name":"WarMachine",
      "jobTitle":"QA"
    },
    {
      "id":9,
      "name":"SpiderMan",
      "jobTitle":"Intern"
    }
  ]
}
```

#### PUT /employee/${id}

Update or replace employee by ID

Body
```json
{
  "name": "String Required - Employee Name",
  "jobTitle": "String Required - Employee Designation",
  "managerId": "Integer Optional - Manager Employee ID, Required if current employee is not Director",
  "replace": "Boolean Optional - Replace old employee with current employee"
}
```

Request
```
PUT /employee/3
body: {
    "name": "BlackPanther",
    "jobTitle": "Lead",
    "managerId": 1,
    "replace": true
}
```

Response
```json
{
    "id": 12,
    "name": "BlackPanther",
    "jobTitle": "Lead",
    "manager": {
        "id": 1,
        "name": "Thor",
        "jobTitle": "Director"
    },
    "colleagues": [
        {
            "id": 4,
            "name": "CaptainAmerica",
            "jobTitle": "Manager"
        },
        {
            "id": 2,
            "name": "IronMan",
            "jobTitle": "Manager"
        }
    ],
    "subordinates": [
      {
        "id":10,
        "name":"BlackWidow",
        "jobTitle":"Developer"
      }
    ]
}
```

### DELETE /employee/${id}

Delete employee by ID

Request
```
DELETE /employee/10
```

Response
```
OK
```
