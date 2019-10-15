# EMS

A Spring Boot project for Employee Management System

## Table of Contents
- [Validation Script](#validation-script)
- [Docker](#docker)
- [Kubernetes](#kubernetes)
- [Database](#database)
    - [Designation](#designation)
    - [Employee](#employee)
- [Example Date](#example-data)
    - [Designation](#designation)
    - [Employee](#employee)
- [API](#api)
    
## Validation Script

### Setup
- Download JQ from [here](https://github.com/stedolan/jq/releases/download/jq-1.6/jq-win64.exe)
- Copy exe file to the GitBash folder (Example: `C:\Users\<username>\AppData\Local\Programs\Git`)

### Run
```
git-bash
./main.sh
```

### Environment Variables

- **SERVER_HOST**: Host of server (Default: localhost)
- **SERVER_PORT**: Port of server (Default: 8080)
- **SERVER_API_PREFIX**: Prefix of server API (Default: /api/v1)

## Docker

### Setup

Download docker from [here](https://download.docker.com/win/stable/31259/Docker%20for%20Windows%20Installer.exe)

Version: 2.0.0.3

### Uninstall

- Uninstall docker for desktop (Leave it running for 15 mins)
- If the process is stuck, force close it from task manager
- Open `regedit` from start
- Remove the following path from registry (Could be different)
```
Computer\HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Docker for Windows
```

### Environment Variables

- **PROFILE**: Spring Boot profile to run in (Default: development)
- **MYSQL_HOST**: MySQL Database Host (Default: mysql)
- **MYSQL_PORT**: MySQL Database Port (Default: 3306)
- **MYSQL_USERNAME**: MySQL Username (Default: root)
- **MYSQL_PASSWORD**: MySQL Password (Default: root)
- **MYSQL_DATABASE**: MySQL Database name to use (Default: ems)


## Kubernetes

### Setup
- Install nginx-ingress

#### Deployment
```
kubectl apply -f https://raw.githubusercontent.com/pepperkick/springboot-ems/master/deployment.yaml
```

## Database

#### Designation
- id: Integer (Primary Key)
- level: Float
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

| id  | name            | manager | designation   |
| --- | --------------- | ------- | ------------- |
| 1   | Thor            | null    | 1 (Director)  |
| 2   | Iron Man        | 1       | 2 (Manager)   |
| 3   | Hulk            | 1       | 3 (Lead)      |
| 4   | Captain America | 1       | 2 (Manager)   |
| 5   | War Machine     | 2       | 6 (QA)        |
| 6   | Vision          | 2       | 5 (DevOps)    |
| 7   | Falcon          | 4       | 4 (Developer) |
| 8   | Ant Man         | 4       | 3 (Lead)      |
| 9   | Spider Man      | 2       | 7 (Intern)    |
| 10  | Black kWidow    | 3       | 4 (Developer) |

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
      "id":1,
      "name":"Thor",
      "jobTitle":"Director",
      "subordinates":[
         {
            "id":4,
            "name":"Captain America",
            "jobTitle":"Manager"
         },
         {
            "id":2,
            "name":"Iron Man",
            "jobTitle":"Manager"
         },
         {
            "id":3,
            "name":"Hulk",
            "jobTitle":"Lead"
         }
      ]
   },
   {
      "id":4,
      "name":"Captain America",
      "jobTitle":"Manager",
      "manager":{
         "id":1,
         "name":"Thor",
         "jobTitle":"Director"
      },
      "colleagues":[
         {
            "id":2,
            "name":"Iron Man",
            "jobTitle":"Manager"
         },
         {
            "id":3,
            "name":"Hulk",
            "jobTitle":"Lead"
         }
      ],
      "subordinates":[
         {
            "id":8,
            "name":"Ant Man",
            "jobTitle":"Lead"
         },
         {
            "id":7,
            "name":"Falcon",
            "jobTitle":"Developer"
         }
      ]
   },
   {
      "id":2,
      "name":"Iron Man",
      "jobTitle":"Manager",
      "manager":{
         "id":1,
         "name":"Thor",
         "jobTitle":"Director"
      },
      "colleagues":[
         {
            "id":4,
            "name":"Captain America",
            "jobTitle":"Manager"
         },
         {
            "id":3,
            "name":"Hulk",
            "jobTitle":"Lead"
         }
      ],
      "subordinates":[
         {
            "id":6,
            "name":"Vision",
            "jobTitle":"DevOps"
         },
         {
            "id":5,
            "name":"War Machine",
            "jobTitle":"QA"
         },
         {
            "id":9,
            "name":"Spider Man",
            "jobTitle":"Intern"
         }
      ]
   }
]
```

### POST /employee

Add a new employee

Body
```json
{
  "name": "String Required - Employee Name",
  "jobTitle": "String Required - Employee Designation",
  "managerId": "Integer Optional - Manager Employee ID, Required if current employee is not Director"
}
```

Request
```
POST /employee
body: {
    "name": "Dr Strange",
    "jobTitle": "Manager",
    "managerId": 1
}
```

Response

```json
{
    "id": 11,
    "name": "Dr Strange",
    "jobTitle": "Manager",
    "manager": {
        "id": 1,
        "name": "Thor",
        "jobTitle": "Director"
    },
    "colleagues": [
        {
            "id": 4,
            "name": "Captain America",
            "jobTitle": "Manager"
        },
        {
            "id": 2,
            "name": "Iron Man",
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
  "id": 2,
  "name": "IronMan",
  "jobTitle": "Manager",
  "manager": {
    "id": 1,
    "name": "Thor",
    "jobTitle": "Director"         
  },
  "colleagues": [
    {
      "id":4,
      "name":"Captain America",
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
      "name":"Vision",
      "jobTitle":"DevOps"
    },
    {
      "id":5,
      "name":"War Machine",
      "jobTitle":"QA"
    },
    {
      "id":9,
      "name":"Spider Man",
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
    "name": "Black Panther",
    "jobTitle": "Lead",
    "managerId": 1,
    "replace": true
}
```

Response
```json
{
    "id": 12,
    "name": "Black Panther",
    "jobTitle": "Lead",
    "manager": {
        "id": 1,
        "name": "Thor",
        "jobTitle": "Director"
    },
    "colleagues": [
        {
            "id": 4,
            "name": "Captain America",
            "jobTitle": "Manager"
        },
        {
            "id": 2,
            "name": "Iron Man",
            "jobTitle": "Manager"
        }
    ],
    "subordinates": [
      {
        "id":10,
        "name":"Black Widow",
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

### GET /designation

Get all designations

Request
```
GET /designation
```

Response
```json
[
    {
        "id": 6,
        "title": "Director",
        "level": 1.0
    },
    {
        "id": 8,
        "title": "Manager",
        "level": 2.0
    },
    {
        "id": 15,
        "title": "Lead",
        "level": 3.0
    },
    {
        "id": 16,
        "title": "Developer",
        "level": 4.0
    },
    {
        "id": 17,
        "title": "DevOps",
        "level": 4.0
    },
    {
        "id": 20,
        "title": "QA",
        "level": 4.0
    },
    {
        "id": 21,
        "title": "Intern",
        "level": 5.0
    }
]
```

### POST /designation

Add new designation

Body
```json
{
  "name": "String Required - Designation Name",
  "higher": "Number Optional - Designation ID Higher to new Designation",
  "equals": "Boolean Optional - Set level equal to higher designation"
}
```

Request
```
POST /designation
body: {
    "name": "DBMS",
    "higher": 20,
    "equals": true
}
```

Response
```json
{
  "id": 22,
  "name": "DBMS",
  "level": 4.0
}
```

### DELETE /designation/${id}

Delete designation by ID

Request
```
DELETE /designation/22
```

Response
```
OK
```
